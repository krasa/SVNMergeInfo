package krasa.core.frontend.commons.table;

import java.util.List;

import krasa.core.frontend.commons.DropDownChoicePanel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.*;

public abstract class DropDownChoiceColumn<T, S> extends AbstractColumn<T, S> {

	private String propertyExpression;

	public DropDownChoiceColumn(IModel<String> displayModel, String propertyExpression1) {
		super(displayModel);
		this.propertyExpression = propertyExpression1;
	}

	@Override
	public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel) {
		item.add(new DropDownChoicePanel(componentId, new PropertyModel<String>(rowModel, propertyExpression),
				getDisplayModel(rowModel)) {

			@Override
			protected void onUpdate(AjaxRequestTarget target, PropertyModel<String> models) {
				DropDownChoiceColumn.this.onUpdate(target, models);
			}
		});
	}

	protected abstract IModel<List<String>> getDisplayModel(IModel<T> rowModel);

	protected void onUpdate(AjaxRequestTarget target, PropertyModel<String> itemModel) {
	}

}
