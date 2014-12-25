package krasa.core.frontend.pages;

import krasa.core.frontend.commons.MyFeedbackPanel;
import krasa.merge.backend.facade.FacadeImpl;
import krasa.merge.frontend.component.SvnProjectsLeftMenuPanel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

public abstract class BasePage extends WebPage {

	public static final String FEEDBACK = "feedback";
	public static final String LEFT = "left";
	@SpringBean
	protected FacadeImpl facade;
	protected WebMarkupContainer centerColumn = new TransparentWebMarkupContainer("center-column");

	// public abstract IModel getPageTitle();

	// public abstract IModel getDescription();
	//
	// public abstract IModel getKeywords();

	protected Component newTopMenuPanel(String id) {
		return new MainMenuPanel(id, this);
	}

	protected Component newLeftColumnPanel(String id) {
		return new SvnProjectsLeftMenuPanel(id);
	}

	protected Component newCurrentPanel(String id) {
		return new EmptyPanel(id);
	}

	@Override
	protected void onBeforeRender() {
		// add the <title> tag
		// addOrReplace(new Label("title", getPageTitle()));

		// Label desc = new Label("description", "");
		// desc.add(new AttributeAppender("content", getDescription(), " "));
		// addOrReplace(desc);
		//
		// Label keywords = new Label("keywords", "");
		// keywords.add(new AttributeAppender("content", getKeywords(), " "));
		// addOrReplace(keywords);
		add(centerColumn);

		if (get("top") == null) {
			// subclass-driven components not yet initilized
			addOrReplace(newTopMenuPanel("top"));
		}
		if (get(LEFT) == null) {
			// subclass-driven components not yet initilized
			addOrReplace(newLeftColumnPanel(LEFT));
		}
		if (get("current") == null) {
			// subclass-driven components not yet initilized
			addOrReplace(newCurrentPanel("current"));
		}

		if (get(FEEDBACK) == null) {
			// subclass-driven components not yet initilized
			FeedbackPanel feedback = new MyFeedbackPanel(FEEDBACK);
			addOrReplace(feedback);
		}

		/** cascades the call to its children */
		super.onBeforeRender();
	}

	public BasePage() {
		super();
		// add(new TinyMceInitBehavior());
		// }));new HeaderContributor(new IHeaderContributor() {
		// public void renderHead(IHeaderResponse response) {
		// response.renderJavascriptReference(TinyMCESettings.javaScriptReference());
		// }
		// }));
		// add(new LogOffLink("logoff"));

	}

	protected BasePage(IModel<?> model) {
		super(model);
	}

	protected BasePage(PageParameters parameters) {
		super(parameters);
	}

	public FeedbackPanel getFeedbackPanel() {
		return (FeedbackPanel) get(FEEDBACK);
	}

	public Panel getLeftColumnPanel() {
		return (Panel) get(LEFT);
	}
}
