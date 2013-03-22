package krasa.build.backend.execution;

import java.util.Collections;

import krasa.build.backend.execution.adapter.ProcessAdapter;
import krasa.build.backend.execution.process.DummyProcess;
import krasa.merge.backend.dto.BuildRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("DUMMY")
@Service
public class DummyProcessBuilder extends ProcessBuilder {
	@Autowired
	AutowireCapableBeanFactory beanFactory;

	@Override
	public ProcessAdapter create(BuildRequest request) {

		StringBufferTail stringBufferTail = new StringBufferTail();

		DummyProcess process = new DummyProcess(stringBufferTail, Collections.<String> emptyList());
		ProcessAdapter processAdapter = new ProcessAdapter(process, request, stringBufferTail);
		process.addListener(processAdapter);

		beanFactory.autowireBean(processAdapter);
		beanFactory.autowireBean(process);

		return processAdapter;
	}

}