package dataset;

import java.util.Set;

import region.CityEvent;

public interface UsersAroundAnEventI {
	public Set<String> process(CityEvent ce);
}
