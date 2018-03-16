# FormatEditText
## English| [中文](https://github.com/dkzwm/FormatEditText/blob/master/README.md) 
FormatEditText can be used as a number formatted text input box, which can be used to format phone numbers, format ID numbers, format bank card numbers, etc.

- For example, specify the mode as `MODE_SIMPLE`, the style as `344`, the placeholder as `-`, manually enter `13012345678`, then it will be formatted as `130-1234-5678`. specify the mode as `MODE_COMPLEX`, the style as `***-****-****`, the mark as `-`, manually enter `13012345678`, then it will be formatted as `130-1234-5678`.
## Features:
 - Support configuration format style
 - Support paste and the cursor will auto follow
 - Automatic append or delete placeholder

## Demo
Download [Demo.apk](https://raw.githubusercontent.com/dkzwm/FormatEditText/master/demo/demo.apk)    
## Snapshot
<img src='snapshot.gif'></img>
## How to used   
#### Gradle
```groovy
repositories {  
    ...
    maven { url 'https://jitpack.io' }  
}

dependencies {  
    compile 'com.github.dkzwm:FormatEditText:0.0.5'
}
``` 
#### In Xml
```
//MODE_COMPLEX
<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:fet_formatStyle="+(**)-***-****-****"
    app:fet_mark="*"
    app:fet_mode="mode_complex"/>

//MODE_SIMPLE
<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:fet_formatStyle="344"
    app:fet_mode="mode_simple"
    app:fet_placeholder=" "/>
```
####  In Java
```
//MODE_COMPLEX
FormattedEditText editText = findViewById(R.id.formattedEditText);
editText.setMode(FormattedEditText.MODE_SIMPLE);
editText.setFormatStyle("344");
editText.setPlaceholder(" ");（manually enter "13012345678", then it will be formatted as "130-1234-5678"）
//MODE_SIMPLE
editText.setMode(FormattedEditText.MODE_SIMPLE);
editText.setMark("*");
editText.setFormatStyle("+(**)-***-****-****");（manually enter "8613012345678"，then it will be formatted as "+(86)-130-1234-5678"）
```
#### Xml属性 
|名称|类型|描述|
|:---:|:---:|:---:|
|fet_mode|enum|Set the mode， `MODE_SIMPLE` and `MODE_COMPLEX`|
|fet_formatStyle|string|Set the format style，When `fet_mode` is `MODE_SIMPLE`, the format can only be a pure number. When `fet_mode` is `MODE_COMPLEX`, the format is an arbitrary format and the `fet_mark` attribute needs to be specified. If not specified then the default is `*`|
|fet_mark|string|Set the mark，Only set when `fet_mode` is `MODE_COMPLEX`, and the length must be 1 (default: `*`)|
|fet_placeholder|string|Set the placeholder，Only set when `fet_mode` is `MODE_SIMPLE`, and the length must be 1 (default: ` `)|

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

