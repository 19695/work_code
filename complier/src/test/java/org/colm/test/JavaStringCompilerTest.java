package org.colm.test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Map;

import org.colm.dynamic.compiler.JavaStringCompiler;
import org.junit.Before;
import org.junit.Test;

/**
 * @author michael
 * @see <a href="https://github.com/barrywang88/compiler">barrywang88/compiler</a>
 */
public class JavaStringCompilerTest {

	JavaStringCompiler compiler;

	@Before
	public void setUp() throws Exception {
		compiler = new JavaStringCompiler();
	}

	static final String SINGLE_JAVA = "/* a single java class to one file */  "
			+ "package org.colm.test;                                            "
			+ "import org.colm.test.*;                            "
			+ "public class UserProxy extends User implements BeanProxy {     "
			+ "    boolean _dirty = false;                                    "
			+ "    public void setId(String id) {                             "
			+ "        super.setId(id);                                       "
			+ "        setDirty(true);                                        "
			+ "    }                                                          "
			+ "    public void setName(String name) {                         "
			+ "        super.setName(name);                                   "
			+ "        setDirty(true);                                        "
			+ "    }                                                          "
			+ "    public void setCreated(long created) {                     "
			+ "        super.setCreated(created);                             "
			+ "        setDirty(true);                                        "
			+ "    }                                                          "
			+ "    public void setDirty(boolean dirty) {                      "
			+ "        this._dirty = dirty;                                   "
			+ "    }                                                          "
			+ "    public boolean isDirty() {                                 "
			+ "        return this._dirty;                                    "
			+ "    }                                                          "
			+ "}                                                              ";

	@Test
	public void testCompileSingleClass() throws Exception {
		Map<String, byte[]> results = compiler.compile("UserProxy.java", SINGLE_JAVA);
		assertEquals(1, results.size());
		assertTrue(results.containsKey("org.colm.test.UserProxy"));
		Class<?> clazz = compiler.loadClass("org.colm.test.UserProxy", results);
		// get method:
		Method setId = clazz.getMethod("setId", String.class);
		Method setName = clazz.getMethod("setName", String.class);
		Method setCreated = clazz.getMethod("setCreated", long.class);
		// try instance:
		Object obj = clazz.newInstance();
		// get as proxy:
		BeanProxy proxy = (BeanProxy) obj;
		assertFalse(proxy.isDirty());
		// set:
		setId.invoke(obj, "A-123");
		setName.invoke(obj, "Fly");
		setCreated.invoke(obj, 123000999);
		// get as user:
		User user = (User) obj;
		assertEquals("A-123", user.getId());
		assertEquals("Fly", user.getName());
		assertEquals(123000999, user.getCreated());
		assertTrue(proxy.isDirty());
	}

	static final String MULTIPLE_JAVA = "/* a single class to many files */   "
			+ "package org.colm.test;                                            "
			+ "import java.util.*;                                            "
			+ "public class Multiple {                                        "
			+ "    List<Bird> list = new ArrayList<Bird>();                   "
			+ "    public void add(String name) {                             "
			+ "        Bird bird = new Bird();                                "
			+ "        bird.name = name;                                      "
			+ "        this.list.add(bird);                                   "
			+ "    }                                                          "
			+ "    public Bird getFirstBird() {                               "
			+ "        return this.list.get(0);                               "
			+ "    }                                                          "
			+ "    public static class StaticBird {                           "
			+ "        public int weight = 100;                               "
			+ "    }                                                          "
			+ "    class NestedBird {                                         "
			+ "        NestedBird() {                                         "
			+ "            System.out.println(list.size() + \" birds...\");   "
			+ "        }                                                      "
			+ "    }                                                          "
			+ "}                                                              "
			+ "/* package level */                                            "
			+ "class Bird {                                                   "
			+ "    String name = null;                                        "
			+ "}                                                              ";

	@Test
	public void testCompileMultipleClasses() throws Exception {
		Map<String, byte[]> results = compiler.compile("Multiple.java", MULTIPLE_JAVA);
		assertEquals(4, results.size());
		assertTrue(results.containsKey("org.colm.test.Multiple"));
		assertTrue(results.containsKey("org.colm.test.Multiple$StaticBird"));
		assertTrue(results.containsKey("org.colm.test.Multiple$NestedBird"));
		assertTrue(results.containsKey("org.colm.test.Bird"));
		Class<?> clzMul = compiler.loadClass("org.colm.test.Multiple", results);
		// try instance:
		Object obj = clzMul.newInstance();
		assertNotNull(obj);
	}
}
