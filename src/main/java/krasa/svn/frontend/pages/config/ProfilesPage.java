package krasa.svn.frontend.pages.config;

import krasa.core.frontend.MySession;
import krasa.core.frontend.pages.BasePage;
import krasa.svn.backend.domain.Profile;

import krasa.svn.backend.facade.SvnFacade;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.StringValue;

/**
 * @author Vojtech Krasa
 */
public class ProfilesPage extends BasePage {

	@SpringBean
	private SvnFacade facade;
	protected IModel<Profile> actualProfile;

	public ProfilesPage() {
		init();
	}

	public ProfilesPage(PageParameters parameters) {
		super(parameters);
		init();
	}

	private void init() {
		initActualProfileModel();
		Form<Profile> form = new Form<>("form");
		queue(form);
		form.add(new Button("newProfile") {

			@Override
			public void onSubmit() {
				Profile newProfile = facade.createNewProfile();
				MySession.get().setCurrentProfile(newProfile.getId());
				// PageParameters parameters = createPageParameters(newProfile);
				// setResponsePage(ConfigurationPage.class, parameters);
			}
		});
		form.add(new Button("copyProfile") {

			@Override
			public void onSubmit() {
				Profile newProfile = facade.copyProfile(actualProfile.getObject());
				MySession.get().setCurrentProfile(newProfile.getId());
				// PageParameters parameters = createPageParameters(newProfile);
				// setResponsePage(ConfigurationPage.class, parameters);
			}
		});
	}

	private void initActualProfileModel() {
		actualProfile = new LoadableDetachableModel<Profile>() {

			@Override
			protected Profile load() {
				StringValue id = getPageParameters().get("id");
				Integer profileId;
				if (id.isNull()) {
					profileId = MySession.get().getCurrentProfileId();
				} else {
					profileId = id.toInteger();
				}
				return facade.getProfileByIdOrDefault(profileId);
			}
		};
	}

	@Override
	protected Component newCurrentPanel(String id) {
		return new ProfileEditPanel(id, actualProfile);
	}

	@Override
	protected Component newLeftColumnPanel(String id) {
		return new ProfileListPanel(id, actualProfile);
	}

	public static PageParameters createPageParameters(Profile profile) {
		PageParameters parameters = new PageParameters();
		parameters.add("id", profile.getId());
		return parameters;
	}
}
