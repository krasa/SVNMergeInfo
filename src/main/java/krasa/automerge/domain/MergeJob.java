package krasa.automerge.domain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.HttpStatus;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import com.google.gag.annotation.remark.Win;
import com.google.gag.enumeration.Outcome;

import krasa.automerge.AutoMergeJobMode;
import krasa.automerge.CommitCommand;
import krasa.automerge.DiffCommand;
import krasa.build.backend.domain.BuildJob;
import krasa.build.backend.domain.Status;
import krasa.build.backend.execution.ProcessStatus;
import krasa.build.backend.execution.process.ProcessStatusListener;
import krasa.core.backend.domain.AbstractEntity;
import krasa.core.backend.utils.MdcUtils;
import krasa.svn.backend.dto.MergeInfoResultItem;
import krasa.svn.backend.dto.MergeJobDto;

@Entity
@Access(AccessType.FIELD)
public class MergeJob extends AbstractEntity implements ProcessStatusListener {

	private static final Logger log = LoggerFactory.getLogger(MergeJob.class);

	@Column
	@Enumerated(EnumType.STRING)
	public volatile Status status = Status.PENDING;
	@Column(name = "merge_from")
	private String from;
	@Column(name = "merge_to")
	private String to;
	@Column
	private String fromPath;
	@Column
	private String toPath;
	@Column
	private String repository;
	@Column
	private long revision;
	@Column
	private String caller;
	@Column
	public Date startTime;
	@Column
	public Date endTime;
	@Column
	@Enumerated(EnumType.STRING)
	private AutoMergeJobMode jobMode = AutoMergeJobMode.ALL;

	private MergeJob() {
	}

	protected MergeJob(String from, String to, String fromPath, String toPath, String repository, long revision,
			String caller, AutoMergeJobMode all) {
		this.from = from;
		this.to = to;
		this.fromPath = fromPath;
		this.toPath = toPath;
		this.repository = repository;
		this.revision = revision;
		this.caller = caller;
		this.jobMode = all;
		this.startTime = new Date();
	}

	public static MergeJob create(MergeInfoResultItem mergeInfoResultItem, SVNLogEntry svnLogEntry,
			AutoMergeJobMode mergeJobMode, String userName) {
		String from = mergeInfoResultItem.getFrom();
		String to = mergeInfoResultItem.getTo();
		String fromPath = mergeInfoResultItem.getFromPath();
		String toPath = mergeInfoResultItem.getToPath();
		String repository = mergeInfoResultItem.getRepository();
		long revision = svnLogEntry.getRevision();
		if (StringUtils.isBlank(userName)) {
			throw new AbortWithHttpErrorCodeException(HttpStatus.SC_PAYMENT_REQUIRED, "Fill your user name!");
		}
		return new MergeJob(from, to, fromPath, toPath, repository, revision, userName, mergeJobMode);
	}

	public static void sort(List<MergeJob> mergeJobs) {
		Collections.sort(mergeJobs, new Comparator<MergeJob>() {

			@Override
			public int compare(MergeJob o1, MergeJob o2) {
				Long revision = o1.revision;
				return revision.compareTo(o2.revision);
			}
		});
	}

	public static MergeJobDto getMergeJobDto(MergeJob mergeJob) {
		if (mergeJob == null) {
			return null;
		}
		MergeJobDto e = new MergeJobDto();
		e.setMergeJobId(mergeJob.getId());
		e.setLogName(mergeJob.getLogFileName());
		e.setAutoMergeJobMode(mergeJob.jobMode);
		e.setFrom(mergeJob.from);
		e.setCaller(mergeJob.caller);
		e.setEndTime(mergeJob.endTime);
		e.setStartTime(mergeJob.startTime);
		e.setTo(mergeJob.to);
		e.setStatus(mergeJob.status.name());
		e.setRevision(mergeJob.revision);
		return e;
	}

	@Win(Outcome.EPIC)
	public void merge() {
		MdcUtils.putLogName(getLogFileName());
		log.info("merging " + this);
		SVNClientManager clientManager = getSvnClientManager();
		File workingCopy = getWorkingCopy();
		try {
			SVNURL from = getFromUrl();
			SVNURL to = getToUrl();

			SVNRevisionRange rangeToMerge = getSvnRevisionRange();

			cleanup(clientManager, workingCopy);

			checkoutOrUpdate(workingCopy, to, clientManager.getUpdateClient());
			SVNDiffClient diffClient = getDiffClient(clientManager);

			merge(from, workingCopy, rangeToMerge, diffClient);

			String commitMessage = getCommitMessage(clientManager, from, rangeToMerge, caller);
			new DiffCommand().diff(clientManager, workingCopy);
			new CommitCommand().commit(clientManager, workingCopy, commitMessage);
			log.info("merge done");
		} catch (SVNException e) {
			log.error("merge failed", e);
			throw new RuntimeException(e);
		} catch (Throwable e) {
			log.error(String.valueOf(e), e);
			throw e;
		} finally {
			log.info("end");
			MdcUtils.removeLogName();
		}
	}

	public String getLogFileName() {
		return "autoMerge_" + jobMode.name() + "_" + to + "_" + id;
	}

	@Win
	public String getRevisionDiff() throws SVNException {
		log.info("getting diff");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SVNClientManager clientManager = getSvnClientManager();
		SVNDiffClient diffClient = getDiffClient(clientManager);
		diffClient.doDiff(getFromUrl(), getSvnRevisionRange().getStartRevision(), getFromUrl(),
				getSvnRevisionRange().getEndRevision(), SVNDepth.INFINITY, true, out);
		return out.toString();
	}

	@Override
	public void onStatusChanged(BuildJob buildJob, ProcessStatus processStatus) {

	}

	public SVNURL getFromUrl() throws SVNException {
		SVNURL reposURL = SVNURL.parseURIEncoded(repository);
		return reposURL.appendPath(fromPath, true);
	}

	public SVNURL getToUrl() throws SVNException {
		SVNURL reposURL = SVNURL.parseURIEncoded(repository);
		return reposURL.appendPath(getToPath(), true);
	}

	public SVNRevisionRange getSvnRevisionRange() {
		long revision = this.revision;
		return new SVNRevisionRange(SVNRevision.create(revision - 1), SVNRevision.create(revision));
	}

	public File getWorkingCopy() {
		return new File("target/temp/" + to);
	}

	private String getCommitMessage(SVNClientManager clientManager, SVNURL from, SVNRevisionRange rangeToMerge,
			String caller) throws SVNException {
		MyISVNLogEntryHandler handler = new MyISVNLogEntryHandler();
		clientManager.getLogClient().doLog(from, new String[] {}, getSvnRevisionRange().getEndRevision(),
				rangeToMerge.getEndRevision(), rangeToMerge.getEndRevision(), false, false, 1, handler);
		String message = handler.message;
		message = message.replace("\n", " ");
		message = message.replace("\r", " ");
		message = message.replace(" => ", "-");

		if (message.startsWith("##")) {
			message = message.substring(2);
		}
		String suffix = " from " + this.from + " by " + caller + " " + message + " ( date=" + handler.date + " rev=" + rangeToMerge.getEndRevision() + " author=" + handler.author + ")";;

		String commitMessage;
		if (jobMode == AutoMergeJobMode.ONLY_MERGE_INFO) {
			commitMessage = "##merge mergeinfo" + suffix;
		} else {
			commitMessage = "##merge" + suffix;
		}
		return commitMessage;
	}

	private void cleanup(SVNClientManager clientManager, File workingCopy) throws SVNException {
		if (workingCopy.exists()) {
			clientManager.getWCClient().doCleanup(workingCopy);
			clientManager.getWCClient().doRevert(new File[] { workingCopy }, SVNDepth.INFINITY, null);
		}
	}

	private SVNDiffClient getDiffClient(SVNClientManager clientManager) {
		SVNDiffClient diffClient = clientManager.getDiffClient();
		// if (getJobMode() == AutoMergeJobMode.ONLY_SVN_MERGE_INFO) {
		// DefaultSVNOptions options = (DefaultSVNOptions) diffClient.getOptions();
		// //This way we set a conflict handler which will automatically resolve conflicts for those
		// //cases that we would like
		// options.setConflictHandler(new ConflictResolverHandler());
		// }
		return diffClient;
	}

	protected void merge(SVNURL from, File workingCopy, SVNRevisionRange rangeToMerge, SVNDiffClient diffClient)
			throws SVNException {
		boolean recordOnly = jobMode == AutoMergeJobMode.ONLY_MERGE_INFO;
		log.info("merging from " + from.getPath() + " into " + workingCopy.getAbsolutePath() + " revision "
				+ rangeToMerge.getStartRevision() + "-" + rangeToMerge.getEndRevision() + " recordOnly=" + recordOnly);

		diffClient.doMerge(from, SVNRevision.HEAD, Collections.singleton(rangeToMerge), workingCopy, SVNDepth.INFINITY,
				true, false, true, recordOnly);
		diffClient.doMerge(from, SVNRevision.HEAD, Collections.singleton(rangeToMerge), workingCopy, SVNDepth.INFINITY,
				true, false, false, recordOnly);
	}

	public SVNClientManager getSvnClientManager() {
		SVNClientManager svnClientManager = SVNClientManager.newInstance();
		svnClientManager.setEventHandler(getHandler());
		return svnClientManager;
	}

	protected ISVNEventHandler getHandler() {
		return new ISVNEventHandler() {

			@Override
			public void handleEvent(SVNEvent event, double progress) throws SVNException {
				SVNStatusType contentsStatus = event.getContentsStatus();
				if (contentsStatus != null && SVNStatusType.CONFLICTED.getID() == contentsStatus.getID()) {
					throw new RuntimeException("CONFLICT, " + event);
				}
				log.info(event.toString());
			}

			@Override
			public void checkCancelled() throws SVNCancelException {

			}
		};
	}

	protected void checkoutOrUpdate(File wcRoot, SVNURL svnurl, SVNUpdateClient updateClient) throws SVNException {
		updateClient.setIgnoreExternals(false);
		if (wcRoot.exists()) {
			log.info("updating " + wcRoot.getAbsolutePath());
			updateClient.doUpdate(wcRoot, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
		} else {
			log.info("checkouting to " + wcRoot.getAbsolutePath());
			updateClient.doCheckout(svnurl, wcRoot, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
		}
	}

	public boolean isSameDestination(MergeJob mergeJob) {
		return mergeJob.getToPath().equals(getToPath());
	}

	public String getToPath() {
		return toPath;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		if (status.isEnd()) {
			endTime = new Date();
		}
	}

	private static class MyISVNLogEntryHandler implements ISVNLogEntryHandler {

		protected String message;
		protected String author;
		protected Date date;

		@Override
		public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
			message = logEntry.getMessage();
			date = logEntry.getDate();
			author = logEntry.getAuthor();
		}
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
