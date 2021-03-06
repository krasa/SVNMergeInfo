package krasa.svn.backend.dto;

import java.io.Serializable;
import java.util.*;

import krasa.automerge.AutoMergeJobMode;
import krasa.automerge.domain.MergeJob;
import krasa.build.backend.DateUtils;

import org.apache.commons.lang3.builder.*;

/**
 * @author Vojtech Krasa
 */
public class MergeJobDto implements Serializable, Comparable<MergeJobDto> {

	public Date startTime;
	public Date endTime;
	private AutoMergeJobMode autoMergeJobMode;
	private String caller;
	private String from;
	private String to;
	private long revision;
	private String status;
	private Integer mergeJobId;
	private String logName;

	public AutoMergeJobMode getAutoMergeJobMode() {
		return autoMergeJobMode;
	}

	public void setAutoMergeJobMode(AutoMergeJobMode autoMergeJobMode) {
		this.autoMergeJobMode = autoMergeJobMode;
	}

	public static List<MergeJobDto> translate(Collection<MergeJob> all) {
		List<MergeJobDto> mergebuildJobs = new ArrayList<>();

		for (MergeJob autoMergeProcess : all) {
			MergeJobDto e = MergeJob.getMergeJobDto(autoMergeProcess);
			mergebuildJobs.add(e);
		}
		Collections.sort(mergebuildJobs);
		return mergebuildJobs;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getMergeJobId() {
		return mergeJobId;
	}

	public void setMergeJobId(Integer mergeJobId) {
		this.mergeJobId = mergeJobId;
	}

	@Override
	public int compareTo(MergeJobDto o) {
		return DateUtils.compareDatesNullOnEnd(this.getStartTime(), o.getStartTime());
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

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}
}
