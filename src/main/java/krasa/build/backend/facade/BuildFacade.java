package krasa.build.backend.facade;

import java.util.List;

import javax.validation.constraints.Null;

import krasa.build.backend.domain.BuildableComponent;
import krasa.build.backend.domain.Environment;
import krasa.build.backend.exception.AlreadyExistsException;
import krasa.build.backend.execution.ProcessStatus;
import krasa.build.backend.execution.adapter.ProcessAdapter;
import krasa.merge.backend.domain.Displayable;
import krasa.merge.backend.dto.BuildRequest;

public interface BuildFacade {
	ProcessAdapter build(BuildRequest request);

	List<Environment> getEnvironments();

	Environment createEnvironment(String environmentName) throws AlreadyExistsException;

	List<BuildableComponent> getComponentsByEnvironment(Environment environment);

	@Null
	ProcessAdapter refresh(BuildRequest buildRequest);

	void addBuildableComponent(Environment object, String branchName);

	void onResult(BuildRequest request, ProcessStatus processStatus);

	void deleteComponent(Environment environment, BuildableComponent object);

	void deleteEnvironment(Integer id);

	void addAllMatchingComponents(Environment object, String fieldValue);

	List<Displayable> getMatchingComponents(String input);

	Environment getEnvironmentByName(String s);
}
