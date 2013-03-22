package krasa.core.frontend.commons.table;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class DummyDataProvider<T extends Serializable> implements ISortableDataProvider<T, String> {

	private List<T> ts;

	public DummyDataProvider(List<T> ts) {
		if (ts == null) {
			throw new IllegalArgumentException();
		}
		this.ts = ts;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends T> iterator(long first, long count) {
		return ts.iterator();
	}

	@Override
	public long size() {
		return ts.size();
	}

	@Override
	public IModel<T> model(T object) {
		return new Model<T>(object);
	}

	@Override
	public ISortState getSortState() {
		return new SingleSortState();
	}
}