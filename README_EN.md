# FormatEditText
## English| [中文](README.md)

 **This library provides three classes for use:**
 - `ClearEditText` Can be used as a text input box with clear function.
 - `FormattedEditText` Can be used as a mask text input box.
 - `MaskNumberEditText` Can be used as a number or amount text input box.

## Features:
 - Support configuration format style
 - Support configuration hint text
 - Support paste and the cursor will auto follow
 - Automatic append or delete placeholder
 - Support for configuration clear icon without occupying the position of CompoundDrawables

## Demo
Download [Demo.apk](https://github.com/dkzwm/FormatEditText/raw/develop/apk/demo.apk)
## Snapshot
<img src='snapshot.gif'></img>
## Installation
Add the following dependency to your build.gradle file:
```
dependencies {
    implementation 'me.dkzwm.widget.fet:core:0.2.0'
}
```
## How to used
#### In Xml
```
<me.dkzwm.widget.fet.FormattedEditText
    android:id="@+id/formattedEditText_simple"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:inputType="phone"
    android:maxLength="13"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_formatStyle="344"
    app:fet_mode="mode_simple"
    app:fet_placeholder="-" />

<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:inputType="phone"
    android:maxLength="19"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_formatStyle="+(86)-***-****-****"
    app:fet_mark="*"
    app:fet_mode="mode_complex" />

<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:inputType="phone"
    android:maxLength="19"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_emptyPlaceholder="_"
    app:fet_formatStyle="+(86)-000-0000-0000"
    app:fet_mode="mode_mask"
    app:fet_showHintWhileEmpty="true" />

<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:digits="0123456789X"
    android:maxLength="22"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_formatStyle="000 000 0000 0000 000*"
    app:fet_hintText="100 000 2020 0101 000X"
    app:fet_mode="mode_hint"
    app:fet_showHintWhileEmpty="false" />

<me.dkzwm.widget.fet.MaskNumberEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_autoFillDecimal="false"
    app:fet_currencySymbol="$"
    app:fet_decimalLength="2"
    app:fet_showThousandsSeparator="true" />
```
####  In Java
```
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_SIMPLE)
        .formatStyle("344")
        .placeholder(' ')
        .config(editText);
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_COMPLEX)
        .formatStyle("+(86)-***-****-****")
        .mark('*')
        .config(editText);
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_MASK)
        .formatStyle("+(86)-000-0000-0000")
        .emptyPlaceholder('_')
        .showHintWhileEmpty(true)
        .config(editText);
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_HINT) //提示模式
        .formatStyle("000 000 0000 0000 000X") //格式化样式
        .maskFilter("X", new FormattedEditText.Matcher() {
            @Override
            public boolean hasMatch(String previousText, String value) {
                return TextUtils.isDigitsOnly(value) || value.toUpperCase().equals("X");
            }
        })
        .maskFilter("0", new FormattedEditText.Matcher() {
            @Override
            public boolean hasMatch(String previousText, String value) {
                return TextUtils.isDigitsOnly(value);
            }
        })
        .hintText("100 000 2020 0101 000X")
        .hintColor(Color.GRAY)
        .showHintWhileEmpty(true)
        .config(editText);
MaskNumberEditText editText = new MaskNumberEditText(context);
editText.setShowThousandsSeparator(true);
editText.setAutoFillDecimal(true);
editText.setDecimalLength(2);
editText.setCurrencySymbol("￥");
```
#### Xml attr
##### ClearEditText
|Name|Format|Desc|
|:---:|:---:|:---:|
|fet_clearDrawable|reference|Set the clear icon|
|fet_drawableGravity|enum|Set the gravity of clear icon，support `GRAVITY_TOP`、`GRAVITY_CENTER`、`GRAVITY_BOTTOM`，(default`GRAVITY_CENTER`)|
|fet_drawablePadding|dimension|Set the padding of clear icon|

##### FormattedEditText
|Name|Format|Desc|
|:---:|:---:|:---:|
|fet_mode|enum|Set the mode， `MODE_SIMPLE`/`MODE_COMPLEX`/`MODE_MASK`/`MODE_HINT`|
|fet_formatStyle|string|Set the format style，When `fet_mode` is `MODE_SIMPLE`, the format can only be a pure number; When `fet_mode` is `MODE_COMPLEX`, the format is an arbitrary format and the `fet_mark` attribute needs to be specified. If not specified then the default is `*`;|
|fet_mark|string|Set the mark，Only set when `fet_mode` is `MODE_COMPLEX`, and the length must be 1 (default: `*`)|
|fet_placeholder|string|Set the placeholder，Only set when `fet_mode` is `MODE_SIMPLE`, and the length must be 1 (default: ` `)|
|fet_emptyPlaceholder|string|Set the empty data placeholder，Only set when `fet_mode`为 `MODE_MASK` , and the length must be 1|
|fet_hintText|string|Set the hint text，Only set when `fet_mode` is `MODE_HINT`, the hint text style must be conform to formatting style|
|fet_hintTextColor|color|Set the hint text color，Only set when `fet_mode` is `MODE_HINT`|
|fet_showHintWhileEmpty|boolean|Set whether to display the default hint text（android:hint）after clearing the data，Only set when `fet_mode` is `MODE_MASK` or `MODE_HINT`|

##### MaskNumberEditText
|Name|Format|Desc|
|:---:|:---:|:---:|
|fet_decimalLength|integer|Specify the length of decimal places|
|fet_currencySymbol|string|Specify the currency symbol|
|fet_currencySymbolTextColor|string|Specify the text color of the currency symbol|
|fet_showThousandsSeparator|boolean|Specify whether to display thousands separator|
|fet_autoFillNumbers|boolean|Specify whether to automatically fill in numbers|
|fet_autoFillNumbersTextColor|boolean|Specify the text color of auto-filled numbers|
#### Mask
`FormattedEditText` When the modes are `MODE_MASK` and` MODE_HINT`, the following characters in the formatting style have special meanings:

 - 0 \- Numeric mask, this will accept only numbers to be typed
 - A \- Letter mask, this will accept only alphabet letters to be typed
 - \* \- Numeric and Letter mask, this will accept numbers and alphabet letters to be typed
 - ? \- Character mask, this will accept anything to be typed
 
Any character that does not have a special meaning will be treated as a literal character and will appear as is in the `FormattedEditText`.
If you need to display these 4 special characters as they are, you need to use the escape character `\`. For example, the internal `0086` of `\\0\\086 000 0000 0000` is displayed as it is during formatting.  

## Thanks
- [reinaldoarrosi—MaskedEditText](https://github.com/reinaldoarrosi/MaskedEditText)   

## License
	--------

    	Copyright (c) 2017 dkzwm

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.

