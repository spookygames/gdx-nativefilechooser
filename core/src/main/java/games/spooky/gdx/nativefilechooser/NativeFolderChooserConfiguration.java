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
package games.spooky.gdx.nativefilechooser;

/**
 * The configuration class for a call to
 * {@link NativeFileChooser#chooseFolder(NativeFolderChooserConfiguration, NativeFolderChooserCallback)}
 * .
 * 
 * <p>
 * Any instance of this is to be considered a plain-old Java object (POJO) and
 * manipulated at will, code in this very library will never modify the content
 * of such instance.
 * 
 * <p>
 * Be careful, not every parameter of a {@code NativeFileChooserConfiguration}
 * may be functional for every implementation of {@link NativeFileChooser}.
 * Consult parameters individually for more details.
 * 
 * @see NativeFileChooser#chooseFolder(NativeFolderChooserConfiguration, NativeFolderChooserCallback)
 * 
 * @see NativeFileChooser
 * @see NativeFileChooserCallback
 * 
 * @author thorthur
 * 
 */
public class NativeFolderChooserConfiguration extends NativeChooserConfiguration {

}
