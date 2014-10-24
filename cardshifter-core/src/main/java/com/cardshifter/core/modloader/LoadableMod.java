
package com.cardshifter.core.modloader;

import java.nio.file.Path;
import java.util.Objects;

import com.cardshifter.modapi.base.ECSGame;

/**
 *
 * @author Frank van Heeswijk
 */
public abstract class LoadableMod implements Mod {
	protected final Path modDirectory;
	
	protected boolean loaded = false;
	
	public LoadableMod(final Path modDirectory) {
		this.modDirectory = Objects.requireNonNull(modDirectory, "modDirectory");
	}
	
	@Override
	public String getName() {
		return modDirectory.getFileName().toString();
	}
	
	final void load() throws ModNotLoadableException {
		if (loaded) {
			throw new IllegalStateException("Mod " + getName() + " has already been loaded");
		}
		load0();
		loaded = true;
	}
	
	final void unload() {
		checkLoaded();
		unload0();
		loaded = false;
	}
	
	@Override
	public void setupGame(ECSGame game) {
		checkLoaded();
		createGame0(game);
	}
	
	@Override
	public void declareConfiguration(ECSGame game) {
		checkLoaded();
		declareConfiguration0(game);
	}
	
	private void checkLoaded() {
		if (!loaded) {
			throw new IllegalStateException("Mod " + getName() + " has not been loaded yet");
		}
	}
	
	protected abstract void load0() throws ModNotLoadableException;
	
	protected abstract void unload0();
	
	protected abstract void createGame0(ECSGame game);
	
	protected abstract void declareConfiguration0(ECSGame game);

}
