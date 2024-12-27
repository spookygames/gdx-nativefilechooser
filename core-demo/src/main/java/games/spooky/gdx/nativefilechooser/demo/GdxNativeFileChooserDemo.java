/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Spooky Games
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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import games.spooky.gdx.nativefilechooser.NativeFilesChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserConfiguration;

@SuppressWarnings("CallToPrintStackTrace")
public class GdxNativeFileChooserDemo extends ApplicationAdapter {

	SpriteBatch batch;

	Stage stage;
	Skin skin;

	Preferences prefs;

	Label fileLabel;
	VerticalGroup files;
	Button saveFileButton;

	final NativeFileChooser fileChooser;
	final ObjectSet<FileHandle> selectedFiles = new ObjectSet<>();

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

		fileLabel = new Label("" , skin);
		fileLabel.setAlignment(Align.center);

		files = new VerticalGroup();

		Button openFileButton = new TextButton("Open audio file", skin);
		openFileButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				NativeFileChooserConfiguration conf = audioChooserConfiguration();
				conf.title = "Select audio file";

				fileChooser.chooseFile(conf, new SelectionCallback());
			}
		});

		Button openFilesButton = new TextButton("Open audio files", skin);
		openFilesButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				NativeFileChooserConfiguration conf = audioChooserConfiguration();
				conf.title = "Select audio files";

				fileChooser.chooseFiles(conf, new SelectionCallback());
			}
		});

		Button openFolderButton = new TextButton("Open all in folder (no filter)", skin);
		openFolderButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				NativeFileChooserConfiguration fileConf = audioChooserConfiguration();
				NativeFolderChooserConfiguration folderConf = new NativeFolderChooserConfiguration();
				folderConf.directory = fileConf.directory;
				folderConf.title = "Select folder";

				fileChooser.chooseFolder(folderConf, new SelectionCallback());
			}
		});

		Button clearFilesButton = new TextButton("Clear audio files", skin);
		clearFilesButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				System.out.println("-- Clear files");
				selectedFiles.clear();
				refresh();
			}
		});

		saveFileButton = new TextButton("Save audio file", skin);
		saveFileButton.setDisabled(true);
		saveFileButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (selectedFiles.isEmpty())
					return;

				NativeFileChooserConfiguration conf = audioChooserConfiguration();
				conf.title = "Select destination";
				conf.intent = NativeFileChooserIntent.SAVE;

				fileChooser.chooseFile(conf, new NativeFileChooserCallback() {
					@Override
					public void onFileChosen(FileHandle file) {
						System.out.println("-- Save first file to: " + file);

						if (selectedFiles.isEmpty())
							return;

						try {
							selectedFiles.first().copyTo(file);
						} catch (Exception exception) {
							onError(exception);
						}

						if (file != null) {
							prefs.putString("last", file.parent().file().getAbsolutePath());
							prefs.flush();
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
		rootTable.row().padTop(20f);
		rootTable.add(fileLabel).grow().colspan(2);
		rootTable.row().padTop(10f);
		rootTable.add(files).colspan(2);
		rootTable.row().padTop(20f).growX();
		rootTable.add(openFileButton);
		rootTable.add(saveFileButton);
		rootTable.row().padTop(4f).growX();
		rootTable.add(openFilesButton);
		rootTable.add();
		rootTable.row().padTop(4f).padBottom(20f).growX();
		rootTable.add(openFolderButton);
		rootTable.add(clearFilesButton);

		stage = new Stage(new ScreenViewport(camera), batch);
		stage.addActor(rootTable);

		Gdx.input.setInputProcessor(stage);

		refresh();
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

	private void refresh() {
		boolean empty = selectedFiles.isEmpty();
		fileLabel.setText(empty ? "Selected audio file: None. Open a file first" : "Selected audio files: " + selectedFiles.size);
		saveFileButton.setDisabled(empty);
		files.clear();
		for (FileHandle file : selectedFiles) {
			files.addActor(new Label(file.path(), skin));
		}
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

	private class SelectionCallback implements NativeFileChooserCallback, NativeFilesChooserCallback, NativeFolderChooserCallback {

		@Override
		public void onFilesChosen(Array<FileHandle> files) {
			System.out.println("-- Files chosen: " + files);

			if (files != null && files.size > 0) {
				prefs.putString("last", files.first().parent().file().getAbsolutePath());
				prefs.flush();
			}

			selectedFiles.addAll(files);
			refresh();
		}

		@Override
		public void onFileChosen(FileHandle file) {
			System.out.println("-- File chosen: " + file);

			if (file != null) {
				prefs.putString("last", file.parent().file().getAbsolutePath());
				prefs.flush();
			}

			selectedFiles.add(file);
			refresh();
		}

		@Override
		public void onCancellation() {
			System.out.println("-- Cancelled");
		}

		@Override
		public void onError(Exception exception) {
			exception.printStackTrace();
			fileLabel.setText(exception.getLocalizedMessage());
		}

		@Override
		public void onFolderChosen(FileHandle folder) {
			System.out.println("-- Folder chosen: " + folder);

			if (folder != null) {
				prefs.putString("last", folder.path());
				prefs.flush();

				selectedFiles.addAll(folder.list());
				refresh();
			}
		}
	}

}
