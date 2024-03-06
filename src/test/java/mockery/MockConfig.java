package mockery;

import com.technicjelle.bluemapsignextractor.common.Config;

public class MockConfig implements Config {
	@Override
	public String getMarkerSetName() {
		return "Signs";
	}

	@Override
	public boolean isToggleable() {
		return true;
	}

	@Override
	public boolean isDefaultHidden() {
		return false;
	}
}
