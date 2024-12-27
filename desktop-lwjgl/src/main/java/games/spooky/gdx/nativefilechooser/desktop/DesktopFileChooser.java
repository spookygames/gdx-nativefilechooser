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
package games.spooky.gdx.nativefilechooser.desktop;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NFDPathSet;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import games.spooky.gdx.nativefilechooser.NativeFileChooserUtils;
import games.spooky.gdx.nativefilechooser.NativeFilesChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserConfiguration;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_Free;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_GetCount;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_GetPath;
import static org.lwjgl.util.nfd.NativeFileDialog.nNFD_Free;

public class DesktopFileChooser implements NativeFileChooser {

	@Override
	public void chooseFile(NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		CharSequence filterList = configuration.mimeFilter == null ? null : createFilterList(configuration.mimeFilter);

		PointerBuffer path = memAllocPointer(1);

		try {
			int result = configuration.intent == NativeFileChooserIntent.SAVE ?
					NativeFileDialog.NFD_SaveDialog(filterList, configuration.directory.file().getPath(), path) :
					NativeFileDialog.NFD_OpenDialog(filterList, configuration.directory.file().getPath(), path);

			switch (result) {
				case NativeFileDialog.NFD_OKAY:
					FileHandle file = new FileHandle(path.getStringUTF8(0));
					callback.onFileChosen(file);
					nNFD_Free(path.get(0));
					break;
				case NativeFileDialog.NFD_CANCEL:
					callback.onCancellation();
					break;
				case NativeFileDialog.NFD_ERROR:
					callback.onError(new Exception(NFD_GetError()));
					break;
			}
		} catch (Exception e) {
			callback.onError(e);
		} finally {
			memFree(path);
		}
	}

	@Override
	public void chooseFiles(NativeFileChooserConfiguration configuration, NativeFilesChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		CharSequence filterList = configuration.mimeFilter == null ? null : createFilterList(configuration.mimeFilter);

		NFDPathSet paths = NFDPathSet.create();

		try {
			int result = NativeFileDialog.NFD_OpenDialogMultiple(filterList, configuration.directory.file().getPath(), paths);

			switch (result) {
				case NativeFileDialog.NFD_OKAY:
					int count = (int) NFD_PathSet_GetCount(paths);
					Array<FileHandle> files = new Array<>(count);
					for (int i = 0; i < count; i++) {
						files.add(new FileHandle(Objects.requireNonNull(NFD_PathSet_GetPath(paths, i))));
					}
					callback.onFilesChosen(files);
					break;
				case NativeFileDialog.NFD_CANCEL:
					callback.onCancellation();
					break;
				case NativeFileDialog.NFD_ERROR:
					callback.onError(new Exception(NFD_GetError()));
					break;
			}
		} catch (Exception e) {
			callback.onError(e);
		} finally {
			NFD_PathSet_Free(paths);
		}
	}

	@Override
	public void chooseFolder(NativeFolderChooserConfiguration configuration, NativeFolderChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		PointerBuffer path = memAllocPointer(1);

		try {
			int result = NativeFileDialog.NFD_PickFolder(configuration.directory.path(), path);

			switch (result) {
				case NativeFileDialog.NFD_OKAY:
					FileHandle file = new FileHandle(path.getStringUTF8(0));
					callback.onFolderChosen(file);
					nNFD_Free(path.get(0));
					break;
				case NativeFileDialog.NFD_CANCEL:
					callback.onCancellation();
					break;
				case NativeFileDialog.NFD_ERROR:
					callback.onError(new Exception(NFD_GetError()));
					break;
			}
		} catch (Exception e) {
			callback.onError(e);
		} finally {
			memFree(path);
		}
	}

	static CharSequence createFilterList(final String mimeType) {
		try {
			return findEligibleMimeTypes(mimeType).stream()
					.flatMap(type -> type.getExtensions().stream().map(s -> s.substring(1)))
					.distinct()
					.collect(Collectors.joining(","));
		} catch (MimeTypeException ignored) {
			return null;
		}
	}

	static Collection<MimeType> findEligibleMimeTypes(final String mimeType) throws MimeTypeException {
		MimeTypes allMimeTypes = MimeTypes.getDefaultMimeTypes();

		if (mimeType.contains("*")) {
			final Pattern mimePattern = NativeFileChooserUtils.mimePattern(mimeType);
			return allMimeTypes.getMediaTypeRegistry().getTypes().stream()
					.map(MediaType::toString)
					.filter(typeName -> mimePattern.matcher(typeName).matches())
					.map(typeName -> {
						try {
							return allMimeTypes.getRegisteredMimeType(typeName);
						} catch (MimeTypeException e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} else {
			return Collections.singletonList(allMimeTypes.forName(mimeType));
		}
	}
}
