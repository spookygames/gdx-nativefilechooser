/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package games.spooky.gdx.nativefilechooser.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

public class GdxNativeFileChooserDemo extends ApplicationAdapter {

	SpriteBatch batch;

	Stage stage;
	Skin skin;

	Preferences prefs;

	Button saveFileButton;

	final NativeFileChooser fileChooser;
	FileHandle selectedFile;

	public GdxNativeFileChooserDemo(NativeFileChooser fileChooser) {
		super();
		this.fileChooser = fileChooser;
	}

	@Override
	public void create() {

		prefs = Gdx.app.getPreferences("GdxNativeFileChooserDemo");

		batch = new SpriteBatch();

		Camera camera = new OrthographicCamera();

		skin = new Skin(Gdx.files.internal("uiskin.json"));

		final Label fileLabel = new Label("Open an audio file first" , skin);
		fileLabel.setAlignment(Align.center);

		Button openFileButton = new TextButton("Open audio file", skin);
		openFileButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				NativeFileChooserConfiguration conf = audioChooserConfiguration();
				conf.title = "Select audio file";

				fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
					@Override
					public void onFileChosen(FileHandle file) {
						selectedFile = file;

						if (file == null) {
							saveFileButton.setDisabled(true);
							fileLabel.setText("Selected audio file: None");
						} else {
							prefs.putString("last", file.parent().file().getAbsolutePath());
							fileLabel.setText("Selected audio file: " + file.path());
							saveFileButton.setDisabled(false);
						}
					}

					@Override
					public void onCancellation() {
						selectedFile = null;
						fileLabel.setText("Selected audio file: None");
					}

					@Override
					public void onError(Exception exception) {
						selectedFile = null;
						exception.printStackTrace();
						fileLabel.setText(exception.getLocalizedMessage());
					}
				});
			}
		});

		Button dirButton = new TextButton("Open dir (LWJGL) - Copies selected file to dir", skin);
		dirButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (selectedFile == null)
					return;

				NativeFileChooserConfiguration conf = audioChooserConfiguration();
				conf.title = "Select destination";
				conf.intent = NativeFileChooserIntent.FOLDER;

				fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
					@Override
					public void onFileChosen(FileHandle file) {
						if (selectedFile == null)
							return;

						try {
							selectedFile.copyTo(file);
						} catch (Exception exception) {
							onError(exception);
						}

						if (file != null) {
							Gdx.app.log("Folder", file.path());
							Gdx.app.log("Folder", file.toString());

						}
					}

					@Override
					public void onCancellation() {
					}

					@Override
					public void onError(Exception exception) {
						exception.printStackTrace();
						fileLabel.setText(exception.getLocalizedMessage());
					}
				});
			}
		});
		saveFileButton = new TextButton("Save audio file", skin);
		saveFileButton.setDisabled(true);
		saveFileButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (selectedFile == null)
					return;

				NativeFileChooserConfiguration conf = audioChooserConfiguration();
				conf.title = "Select destination";
				conf.intent = NativeFileChooserIntent.SAVE;

				fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
					@Override
					public void onFileChosen(FileHandle file) {
						if (selectedFile == null)
							return;

						try {
							selectedFile.copyTo(file);
						} catch (Exception exception) {
							onError(exception);
						}

						if (file != null) {
							prefs.putString("last", file.parent().file().getAbsolutePath());
						}
					}

					@Override
					public void onCancellation() {
					}

					@Override
					public void onError(Exception exception) {
						exception.printStackTrace();
						fileLabel.setText(exception.getLocalizedMessage());
					}
				});
			}
		});

		Table rootTable = new Table(skin);
		rootTable.setFillParent(true);
		rootTable.row();
		rootTable.add(fileLabel).grow().padTop(20f).colspan(2);
		rootTable.row().padBottom(20f).growX();
		rootTable.add(openFileButton);
		rootTable.add(saveFileButton);
		rootTable.add(dirButton);

		stage = new Stage(new ScreenViewport(camera), batch);
		stage.addActor(rootTable);

		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime();

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		batch.dispose();
		stage.dispose();
		skin.dispose();
	}

	private NativeFileChooserConfiguration audioChooserConfiguration() {

		NativeFileChooserConfiguration conf = new NativeFileChooserConfiguration();
		conf.directory = Gdx.files.absolute(prefs.getString("last",
				Gdx.files.isExternalStorageAvailable() ?
						Gdx.files.getExternalStoragePath()
						: (Gdx.files.isLocalStorageAvailable() ?
						Gdx.files.getLocalStoragePath()
						: System.getProperty("user.home"))));
		conf.nameFilter = new FilenameFilter() {
			final String[] extensions = { "wav", "mp3", "ogg" };

			@Override
			public boolean accept(File dir, String name) {
				int i = name.lastIndexOf('.');
				if (i > 0 && i < name.length() - 1) {
					String desiredExtension = name.substring(i + 1).toLowerCase(Locale.ENGLISH);
					for (String extension : extensions) {
						if (desiredExtension.equals(extension)) {
							return true;
						}
					}
				}
				return false;
			}
		};
		conf.mimeFilter = "audio/*";

		return conf;
	}

}
