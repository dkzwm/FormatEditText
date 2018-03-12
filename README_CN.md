# FormatEditText
## [English](https://github.com/dkzwm/FormatEditText/blob/master/README.md) | 中文
<p>
FormatEditText可以用来当做号码格式化文本输入框使用，可以用来作为格式化手机号码、格式化身份证号码、格式化银行卡号码等。    
例如:指定样式为`344`，插入字符为`-`，手动依次输入`13012345678`，那么最终会格式化为`134-1234-5678`，且中间删除更改会自动补位。
<p/>
支持2个属性    
- 1.格式化样式，例如:"344";    
- 2.需要中间插入的字符，例如:" ";    

## Gradle
```groovy
repositories {  
    ...
    maven { url 'https://jitpack.io' }  
}

dependencies {  
    compile 'com.github.dkzwm:FormatEditText:0.0.2’
}
``` 

## Snapshot
<img src='snapshot.gif'></img>

License
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
