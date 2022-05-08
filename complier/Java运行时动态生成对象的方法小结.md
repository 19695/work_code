# Java运行时动态生成对象的方法小结

 更新时间：2021年08月31日 11:24:58  作者：BarryW  

Java是一门静态语言，通常，我们需要的class在编译的时候就已经生成了，为什么有时候我们还想在运行时动态生成class呢？今天通过本文给大家分享Java运行时动态生成对象的方法小结，需要的朋友参考下吧

**目录**

- <a href="#a">一、利用JDK自带工具类实现</a>
- <a href="#b">二、利用三方Jar包实现</a>
- <a href="c">三、利用Groovy脚本实现</a>

最近一个项目中利用规则引擎，提供用户拖拽式的灵活定义规则。这就要求根据数据库数据动态生成对象处理特定规则的逻辑。如果手写不仅每次都要修改代码，还要每次测试发版，而且无法灵活根据用户定义的规则动态处理逻辑。所以想到将公共逻辑写到父类实现，将特定逻辑根据字符串动态生成子类处理。这就可以一劳永逸解决这个问题。

　　那就着手从Java如何根据字符串模板在运行时动态生成对象。

　　Java是一门静态语言，通常，我们需要的class在编译的时候就已经生成了，为什么有时候我们还想在运行时动态生成class呢？

　　经过一番网上资料查找，由繁到简的方式总结如下：



## 一、利用JDK自带工具类实现<a name="a"></a>

　　现在问题来了，动态生成字节码，难度有多大？
　　如果我们要自己直接输出二进制格式的字节码，在完成这个任务前，必须先认真阅读[JVM规范第4章](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html)，详细了解class文件结构。估计读完规范后，两个月过去了。
　　所以，第一种方法，自己动手，从零开始创建字节码，理论上可行，实际上很难。
　　第二种方法，使用已有的一些能操作字节码的库，帮助我们创建class。
　　目前，能够操作字节码的开源库主要有[CGLib](https://github.com/cglib/cglib)和[Javassist](http://jboss-javassist.github.io/javassist/)两种，它们都提供了比较高级的API来操作字节码，最后输出为class文件。
比如CGLib，典型的用法如下：

```java
Enhancer e = new Enhancer();
e.setSuperclass(...);
e.setStrategy(new DefaultGeneratorStrategy() {
    protected ClassGenerator transform(ClassGenerator cg) {
        return new TransformingGenerator(cg,
                   new AddPropertyTransformer(new String[]{ "foo" }, new Class[] { Integer.TYPE }));
    }
});
Object obj = e.create();
```

比自己生成class要简单，但是，要学会它的API还是得花大量的时间，并且，上面的代码很难看懂对不对？

有木有更简单的方法？

有！

Java的编译器是`javac`，但是，在很早很早的时候，Java的编译器就已经用纯Java重写了，自己能编译自己，行业黑话叫“自举”。从Java 1.6开始，编译器接口正式放到JDK的公开API中，于是，我们不需要创建新的进程来调用`javac`，而是直接使用编译器API来编译源码。

使用起来也很简单：

```java
JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
int compilationResult = compiler.run(null, null, null, '/path/Test.java');
```

这么写编译是没啥问题，问题是我们在内存中创建了Java代码后，必须先写到文件，再编译，最后还要手动读取class文件内容并用一个ClassLoader加载。

有木有更简单的方法？

有！

其实Java编译器根本不关心源码的内容是从哪来的，你给它一个`String`当作源码，它就可以输出`byte[]`作为class的内容。

所以，我们需要参考Java Compiler API的文档，让Compiler直接在内存中完成编译，输出的class内容就是`byte[]`。

```java
Map<String, byte[]> results;JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
    JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
    CompilationTask task = compiler.getTask(null, manager, null, null, null, 
                                            Arrays.asList(javaFileObject));
    if (task.call()) {
        results = manager.getClassBytes();
    }
}
```

上述代码的几个关键在于：

用`MemoryJavaFileManager`替换JDK默认的`StandardJavaFileManager`，以便在编译器请求源码内容时，不是从文件读取，而是直接返回`String`；用`MemoryOutputJavaFileObject`替换JDK默认的`SimpleJavaFileObject`，以便在接收到编译器生成的`byte[]`内容时，不写入class文件，而是直接保存在内存中。

最后，编译的结果放在`Map<String, byte[]>`中，Key是类名，对应的`byte[]`是class的二进制内容。

为什么编译后不是一个`byte[]`呢？

因为一个`.java`的源文件编译后可能有多个`.class`文件！只要包含了静态类、匿名类等，编译出的class肯定多于一个。

如何加载编译后的class呢？

加载class相对而言就容易多了，我们只需要创建一个`ClassLoader`，覆写`findClass()`方法：

```java
class MemoryClassLoader extends URLClassLoader {
    Map<String, byte[]> classBytes = new HashMap<String, byte[]>();
    
    public MemoryClassLoader(Map<String, byte[]> classBytes) {
        super(new URL[0], MemoryClassLoader.class.getClassLoader());
        this.classBytes.putAll(classBytes);
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] buf = classBytes.get(name);
        if (buf == null) {
            return super.findClass(name);
        }
        classBytes.remove(name);
        return defineClass(name, buf, 0, buf.length);
    }
}
```

总结以上，那么我们来编写一个Java脚本引擎吧：

https://github.com/barrywang88/compiler

https://github.com/barrywang88/compiler.git



## 二、利用三方Jar包实现<a name="b"></a>

利用三方包com.itranswarp.compiler来实现：

1. 引入Maven依赖包：

```xml
<dependency>
	<groupId>com.itranswarp</groupId>
    <artifactId>compiler</artifactId>
    <version>1.0</version>
</dependency>
```

2. 编写工具类

```java
public class StringCompiler {
	public static Object run(String source, String...args) throws Exception {
        // 声明类名    
        String className = "Main";
        String packageName = "top.fomeiherz";
        // 声明包名：package top.fomeiherz;
        String prefix = String.format("package %s;", packageName);
        // 全类名：top.fomeiherz.Main
        String fullName = String.format("%s.%s", packageName, className);
        // 编译器
        JavaStringCompiler compiler = new JavaStringCompiler();
        // 编译：compiler.compile("Main.java", source)
        Map<String, byte[]> results = compiler.compile(className + ".java", prefix + source);
        // 加载内存中byte到Class<?>对象
        Class<?> clazz = compiler.loadClass(fullName, results);
        // 创建实例
        Object instance = clazz.newInstance();
        Method mainMethod = clazz.getMethod("main", String[].class);
        // String[]数组时必须使用Object[]封装
        // 否则会报错：java.lang.IllegalArgumentException: wrong number of arguments
        return mainMethod.invoke(instance, new Object[]{args});
    }
}
```

3. 测试执行

```java
public class StringCompilerTest {
    public static void main(String[] args) throws Exception {
        // 传入String类型的代码
        String source = "import java.util.Arrays;public class Main" +
            "{" +
            "public static void main(String[] args) {" +
            "System.out.println(Arrays.toString(args));" +
            "}" +
            "}";
        StringCompiler.run(source, "1", "2");
    }
}
```



## 三、利用Groovy脚本实现<a name="c"></a>

以上两种方式尝试过，后来发现Groovy原生就支持脚本动态生成对象。

1. 引入Groovy maven依赖

```xml
<dependency>
    <groupId>org.codehaus.groovy</groupId>
    <artifactId>groovy-all</artifactId>
    <version>2.4.13</version>
</dependency>
```

2. 直接上测试代码

```java
@Test
public void testGroovyClasses() throws Exception {
    //groovy提供了一种将字符串文本代码直接转换成Java Class对象的功能
    GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    //里面的文本是Java代码,但是我们可以看到这是一个字符串我们可以直接生成对应的Class<?>对象,而不需要我们写一个.java文件
    Class<?> clazz = groovyClassLoader.parseClass("package com.xxl.job.core.glue;\n" +
		"\n" +
        "public class Main {\n" +
        "\n" +
        "  public int age = 22;\n" +
        "  \n" +
        "  public void sayHello() {\n" +
        "    System.out.println(\"年龄是:\" + age);\n" +
        "  }\n" +
        "}\n");    
    Object obj = clazz.newInstance();
    Method method = clazz.getDeclaredMethod("sayHello");
    method.invoke(obj);
    Object val = method.getDefaultValue();
    System.out.println(val);
}
```



---



原作者：BarryW 

原帖地址：https://www.jb51.net/article/221299.htm

> 目前我还不能完全理解，我需要把 ClassLoader 搞明白了才能进一步理解