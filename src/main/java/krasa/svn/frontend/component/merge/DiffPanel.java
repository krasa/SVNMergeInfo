package krasa.svn.frontend.component.merge;

import krasa.core.frontend.components.BasePanel;
import krasa.svn.backend.dto.MergeInfoResultItem;
import krasa.svn.backend.service.MergeService;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.*;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.tmatesoft.svn.core.SVNLogEntry;

/**
 * @author Vojtech Krasa
 */
public class DiffPanel extends BasePanel {

	@SpringBean
	protected MergeService mergeService;
	private ModalWindow modal;

	public DiffPanel(String markupId, ModalWindow modal1, MergeInfoResultItem mergeInfoResultItem,
			SVNLogEntry revisionObject) {
		super(markupId);
		modal = modal1;

		Label diff = new Label("diff", getDiffModel(mergeInfoResultItem, revisionObject));
		diff.setOutputMarkupPlaceholderTag(true);
		add(diff);
		Form form = new Form("form");
		add(form);
		form.add(new MergeButton(mergeInfoResultItem, revisionObject));
		form.add(new MergeSvnMergeInfoOnlyButton(mergeInfoResultItem, revisionObject));
		form.add(new AjaxButton("close") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				modal.close(target);
			}
		});
	}

	protected IModel<String> getDiffModel(final MergeInfoResultItem mergeInfoResultItem, final SVNLogEntry svnLogEntry) {
		IModel<String> diff = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return mergeService.getDiff(mergeInfoResultItem, svnLogEntry);
			}
		};
		return diff;
	}

	protected void onMerged(SVNLogEntry revision, AjaxRequestTarget target) {

	}

	private class MergeButton extends AjaxButton {

		private final MergeInfoResultItem mergeInfoResultItem;
		private final SVNLogEntry revisionObject;

		public MergeButton(MergeInfoResultItem mergeInfoResultItem, SVNLogEntry revisionObject) {
			super("merge");
			this.mergeInfoResultItem = mergeInfoResultItem;
			this.revisionObject = revisionObject;
		}

		@Override
		protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
			mergeService.merge(mergeInfoResultItem, revisionObject);
			onMerged(revisionObject, target);
			modal.close(target);
		}
	}

	private class MergeSvnMergeInfoOnlyButton extends AjaxButton {

		private final MergeInfoResultItem mergeInfoResultItem;
		private final SVNLogEntry revisionObject;

		public MergeSvnMergeInfoOnlyButton(MergeInfoResultItem mergeInfoResultItem, SVNLogEntry revisionObject) {
			super("mergeSvnMergeInfoOnly");
			this.mergeInfoResultItem = mergeInfoResultItem;
			this.revisionObject = revisionObject;
		}

		@Override
		protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
			mergeService.mergeSvnMergeInfoOnly(mergeInfoResultItem, revisionObject);
			onMerged(revisionObject, target);
			modal.close(target);
		}
	}
}