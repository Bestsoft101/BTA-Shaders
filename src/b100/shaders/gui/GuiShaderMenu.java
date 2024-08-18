package b100.shaders.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import b100.shaders.ShaderMod;
import b100.shaders.gui.util.ActionListener;
import net.minecraft.core.util.helper.Utils;

public class GuiShaderMenu extends GuiScreen implements ActionListener {

	public GuiScrollPane scrollPane;
	public GuiScrollBar scrollBar;
	public GuiBackgroundBox topBorder;
	public GuiBackgroundBox bottomBorder;
	
	public GuiTextElement titleText;
	
	public GuiButton buttonOpenFolder;
	public GuiButton buttonDone;
	
	private Map<String, File> idToShaderpackMap = new HashMap<>();
	private Set<String> shaderPacks = new HashSet<>();
	private List<String> tempShaderPacks = new ArrayList<>();
	
	private long sinceLastUpdate = 0;
	
	public GuiShaderMenu(IGuiScreen parentScreen) {
		super(parentScreen);
	}
	
	@Override
	public void draw(float partialTicks) {
		super.draw(partialTicks);
		
		update(false);
	}

	@Override
	public void onInit() {
		scrollPane = add(new GuiScrollPane(this));
		
		topBorder = add(new GuiBackgroundBox());
		bottomBorder = add(new GuiBackgroundBox());
		
		add(new GuiShadow(scrollPane, true, false));
		add(new GuiShadow(scrollPane, true, true));
		
		scrollBar = add(new GuiScrollBar(scrollPane));
		
		titleText = add(new GuiTextElement("Shaderpacks"));
		
		buttonOpenFolder = add(new GuiButton(this, "Open Shaderpacks Folder", this));
		buttonDone = add(new GuiButton(this, "Done", this));
		
		update(true);
	}
	
	public void update(boolean force) {
		long now = System.currentTimeMillis();
		if(now - sinceLastUpdate < 1000 && !force) {
			return;
		}
		sinceLastUpdate = now;
		
		boolean filesChanged = false;
		
		tempShaderPacks.clear();
		idToShaderpackMap.clear();
		
		File[] files = ShaderMod.getShaderPackDirectory().listFiles();
		for(int i=0; i < files.length; i++) {
			File file = files[i];
			
			if(ShaderMod.isShaderPack(file)) {
				String id = file.getName() + file.isDirectory() + file.lastModified() + file.length();
				tempShaderPacks.add(id);
				idToShaderpackMap.put(id, file);
			}
		}
		
		if(tempShaderPacks.size() != shaderPacks.size()) {
			filesChanged = true;
		}else {
			for(int i=0; i < tempShaderPacks.size(); i++) {
				String id = tempShaderPacks.get(i);
				if(!shaderPacks.contains(id)) {
					filesChanged = true;
					break;
				}
			}
		}
		
		if(!filesChanged) {
			return;
		}
		
		shaderPacks.clear();
		shaderPacks.addAll(tempShaderPacks);
		
		FocusGroup focusGroup = new FocusGroup();
		scrollPane.elements.clear();
		scrollPane.elements.add(new ShaderPackButton(this, null));
		
		for(int i=0; i < tempShaderPacks.size(); i++) {
			File file = idToShaderpackMap.get(tempShaderPacks.get(i));
			ShaderPackButton shaderPackButton = new ShaderPackButton(this, file);
			scrollPane.elements.add(shaderPackButton);
		}
		
		for(int i=0; i < scrollPane.elements.size(); i++) {
			if(scrollPane.elements.get(i) instanceof ShaderPackButton) {
				ShaderPackButton shaderPackButton = (ShaderPackButton) scrollPane.elements.get(i);
				focusGroup.add(shaderPackButton);
				if(ShaderMod.isShaderPackSelected(shaderPackButton.file)) {
					shaderPackButton.setFocused(true);
				}
			}
		}
		
		onResize();
	}
	
	@Override
	public void onResize() {
		int top = 24;
		int bottom = 30;
		int padding = Math.max(0, (this.width - 250) / 2);
		
		topBorder.setPosition(0, 0).setSize(width, top);
		bottomBorder.setPosition(0, height - bottom).setSize(width, bottom);
		
		scrollPane.setPosition(0, top).setSize(width, height - top - bottom);
		scrollPane.paddingLeft = scrollPane.paddingRight = padding;
		scrollBar.setPosition(scrollPane.posX + scrollPane.width - padding, scrollPane.posY).setSize(5, scrollPane.height);
		
		titleText.setPositionAndSize(topBorder);
		
		{
			int w = 150;
			int p = 3;
			int x0 = width / 2 - p - 150;
			int x1 = width / 2 + p;
			int y = bottomBorder.posY + (bottomBorder.height - 20) / 2;
			buttonOpenFolder.setPosition(x0, y).setSize(w, 20);
			buttonDone.setPosition(x1, y).setSize(w, 20);
		}
		
		super.onResize();
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		
		ShaderMod.config.save();
	}
	
	public static class ShaderPackButton extends GuiFocusButton {

		public final File file;
		
		public ShaderPackButton(GuiScreen screen, File file) {
			super(screen);
			this.file = file;
			
			if(file != null) {
				this.text = file.getName();	
			}else {
				this.text = "(None)";
			}
		}
		
		@Override
		public void onFocusChanged() {
			super.onFocusChanged();
			
			if(isFocused() && screen.isInitialized()) {
				ShaderMod.setShaderpack(file);
			}
		}
		
	}

	@Override
	public void actionPerformed(GuiElement source) {
		if(source == buttonOpenFolder) {
			Utils.openDirectory(ShaderMod.getShaderPackDirectory());
		}
		if(source == buttonDone) {
			back();
		}
	}

}
