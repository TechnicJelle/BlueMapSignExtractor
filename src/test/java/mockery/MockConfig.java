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

	@Override
	public double getMaxDistance() {
		return 0.0;
	}

	@Override
	public boolean areWarningsAllowed() {
		return true;
	}

	@Override
	public boolean areBlankSignsIgnored() {
		return false;
	}
}
