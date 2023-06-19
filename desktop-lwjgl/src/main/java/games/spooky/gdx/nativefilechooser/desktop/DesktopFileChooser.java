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
package games.spooky.gdx.nativefilechooser.desktop;

import com.badlogic.gdx.files.FileHandle;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.stream.Collectors;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError;
import static org.lwjgl.util.nfd.NativeFileDialog.nNFD_Free;

public class DesktopFileChooser implements NativeFileChooser {

	static CharSequence createFilterList(final String mimeType) throws MimeTypeException {
		MimeType type = MimeTypes.getDefaultMimeTypes().forName(mimeType);
		return type.getExtensions().stream().map(s -> s.substring(1)).collect(Collectors.joining(","));
	}

	@Override
	public void chooseFile(NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		CharSequence filterList = null;

		if (configuration.mimeFilter != null) {
			try {
				filterList = createFilterList(configuration.mimeFilter);
			} catch (MimeTypeException ignored) {
			}
		}

		PointerBuffer path = memAllocPointer(1);

		try {
			int result = NativeFileDialog.NFD_OpenDialog(filterList, configuration.directory.file().getPath(), path);

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
}
