package b100.shaders;

public interface CustomRenderer {
	
	public boolean beforeSetupCameraTransform(float partialTicks);
	
	public void afterSetupCameraTransform(float partialTicks);
	
	public void beginRenderBasic();
	
	public void beginRenderTextured();
	
	public void beginRenderSkyBasic();
	
	public void beginRenderSkyTextured();
	
	public void beginRenderTerrain();
	
	public void beginRenderEntities();
	
	public void beginRenderTranslucent();
	
	public void beginRenderClouds();
	
	public void beginRenderHand();
	
	public void onClearWorldBuffer();
	
}
