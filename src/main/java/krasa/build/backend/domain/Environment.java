package krasa.build.backend.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import krasa.core.backend.domain.AbstractEntity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Entity
public class Environment extends AbstractEntity implements Serializable {
	public static Object NAME = "name";

	@Column(unique = true)
	protected String name;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "environment", orphanRemoval = true)
	private List<BuildableComponent> buildableComponetns;

	public Environment() {
	}

	public Environment(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BuildableComponent> getBuildableComponetns() {
		if (buildableComponetns == null) {
			buildableComponetns = new ArrayList<BuildableComponent>();
		}
		return buildableComponetns;
	}

	public void setBuildableComponetns(List<BuildableComponent> buildableComponetns) {
		this.buildableComponetns = buildableComponetns;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public void replaceBuilds(ArrayList<BuildableComponent> buildableComponents) {
		HashMap<String, BuildableComponent> newBuilds = toMap(buildableComponents);

		List<BuildableComponent> builds = getBuildableComponetns();
		for (int i = 0; i < builds.size(); i++) {
			BuildableComponent oldBuild = builds.get(i);
			BuildableComponent newBuild = newBuilds.get(oldBuild.getName());
			if (newBuild != null) {
				builds.set(i, newBuild);
				newBuilds.remove(oldBuild.getName());
			}
		}

		for (BuildableComponent buildableComponent : newBuilds.values()) {
			builds.add(buildableComponent);
		}
	}

	private HashMap<String, BuildableComponent> toMap(ArrayList<BuildableComponent> buildableComponents) {
		HashMap<String, BuildableComponent> hashMap = new HashMap<String, BuildableComponent>();
		for (BuildableComponent buildableComponent : buildableComponents) {
			hashMap.put(buildableComponent.getName(), buildableComponent);
		}
		return hashMap;
	}

	public void add(BuildableComponent component) {
		buildableComponetns.add(component);
		component.setEnvironment(this);
	}
}
