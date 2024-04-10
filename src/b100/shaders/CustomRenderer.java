package b100.shaders;

public interface CustomRenderer {
	
	public boolean beforeSetupCameraTransform(float partialTicks);
	
	public void afterSetupCameraTransform(float partialTicks);
	
	public void beginRenderSkyBasic();
	
	public void beginRenderSkyTextured();
	
	public void beginRenderTerrain();
	
	public void onClearWorldBuffer();
	
}
