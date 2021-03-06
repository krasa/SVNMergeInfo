package krasa.svn.backend.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.tmatesoft.svn.core.SVNDirEntry;

import krasa.core.backend.domain.AbstractEntity;

/**
 * @author Vojtech Krasa
 */
@Entity
@Table(indexes = { @javax.persistence.Index(columnList = "name", unique = false),
		@javax.persistence.Index(columnList = "type, name", unique = false) })
public class SvnFolder extends AbstractEntity implements Displayable {

	public static final Comparator<SvnFolder> NAME_COMPARATOR = new Comparator<SvnFolder>() {

		@Override
		public int compare(SvnFolder o1, SvnFolder o2) {
			return o2.getName().compareTo(o1.getName());
		}
	};
	@Column
	private String name;
	@Column
	private String searchFrom;
	@Column
	private String path;
	@Enumerated
	private Type type;
	@OneToMany(mappedBy = "parent", orphanRemoval = true)
	@Cascade(CascadeType.DELETE)
	private List<SvnFolder> childs;
	@ManyToOne
	private SvnFolder parent;
	@ManyToOne(optional = true)
	private Repository repository;

	public SvnFolder() {
	}

	public SvnFolder(SVNDirEntry entry, String path, Type type) {
		name = entry.getName();
		this.path = path;
		setType(type);
	}

	public static SvnFolder createTrunk(String projectName, String path) {
		SvnFolder svnFolder = new SvnFolder();
		svnFolder.setNameAsTrunk(projectName);
		svnFolder.setPath(path);
		svnFolder.setType(Type.TRUNK);
		return svnFolder;
	}

	public void setNameAsTrunk(String projectName) {
		setName(projectName + " [trunk]");
	}

	public void add(SvnFolder branch) {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		childs.add(branch);
		branch.setParent(this);
	}

	public String getSearchFrom() {
		return searchFrom;
	}

	public void setSearchFrom(String searchFrom) {
		this.searchFrom = searchFrom;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<SvnFolder> getChilds() {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		return childs;
	}

	public void setChilds(List<SvnFolder> childs) {
		this.childs = childs;
	}

	public SvnFolder getParent() {
		return parent;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setParent(SvnFolder parent) {
		this.parent = parent;
	}

	public void removeChild(SvnFolder svnFolder) {
		childs.remove(svnFolder);
	}

	public Set<String> getChildNamesAsSet() {
		Set<String> svnFolders = new HashSet<>();
		for (SvnFolder child : childs) {
			svnFolders.add(child.getName());
		}
		return svnFolders;
	}

	public Map<String, SvnFolder> getChildsAsMapByName() {
		Map<String, SvnFolder> svnFolders = new HashMap<>();
		for (SvnFolder child : childs) {
			svnFolders.put(child.getName(), child);
		}
		return svnFolders;
	}

	public Set<String> getCommonSubFolders(SvnFolder from) {
		Set<String> subFoldersTo = getChildNamesAsSet();
		Set<String> subFolders = from.getChildNamesAsSet();
		Set<String> commonFolders = new HashSet<>();
		for (String s : subFoldersTo) {
			if (subFolders.contains(s)) {
				commonFolders.add(s);
			}
		}
		return commonFolders;
	}

	public boolean childAlreadyExists(SvnFolder branch) {
		Set<String> branchNamesAsSet = getChildNamesAsSet();
		return branchNamesAsSet.contains(branch.getName());
	}

	@Override
	public String getDisplayableText() {
		return getName();
	}

	public boolean nameMatches(List<String> regex) {
		for (String s : regex) {
			if (getName().matches(s)) {
				return true;
			}
		}
		return false;
	}
}
