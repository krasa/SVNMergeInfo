package krasa.build.frontend.components;

import krasa.build.backend.domain.BuildJob;
import krasa.build.backend.facade.BuildFacade;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class PocessRerunButton extends AjaxButton {
	private IModel<BuildJob> model;

	@SpringBean
	protected BuildFacade facade;

	public PocessRerunButton(String id, IModel<BuildJob> model) {
		super(id, new Model<>("Rerun"));
		this.model = model;
	}

	@Override
	protected void onConfigure() {
		BuildJob object = model.getObject();
		if (object != null) {
			this.setEnabled(!object.isProcessAlive());
		}
		super.onConfigure();
	}

	@Override
	protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		facade.build(model.getObject().getBuildableComponent());
		this.setEnabled(false);
		target.add(this);
		super.onSubmit();
	}
}
