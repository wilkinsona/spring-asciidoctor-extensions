package io.spring.asciidoctor.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.InlineMacroProcessor;

import io.spring.asciidoctor.java.JavaIndex.ClassDescriptor;
import io.spring.asciidoctor.java.JavaIndex.MethodDescriptor;

public class JavaInlineMacroProcessor extends InlineMacroProcessor {

	private static final String UNICODE_ELLIPSIS = "&#8230;";

	private static final String UNICODE_NON_BREAKING_SPACE = "&#8203;";

	private final Map<String, JavaIndex> indexes = new HashMap<>();

	private final Map<String, BiFunction<JavaIndex, JavadocLinkResolver, Handler>> handlers = new HashMap<>();

	public JavaInlineMacroProcessor() {
		super("java", Collections.singletonMap("content_model", ":attributes"));
		handlers.put("method", MethodHandler::new);
		handlers.put("class", ClassHandler::new);
	}

	@Override
	protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
		String basePackage = (String)parent.getAttr("java-base-package");
		String javadocUrl = (String)parent.getAttr("javadoc-url");
		javadocUrl = javadocUrl.endsWith("/") ? javadocUrl : javadocUrl + "/";
		JavaIndex javaIndex = indexes.computeIfAbsent(basePackage, JavaIndex::new);
		return handlers.get(target).apply(javaIndex, new JavadocLinkResolver(javadocUrl)).handle(attributes);
	}

	private interface Handler {

		String handle(Map<String, Object> attributes);

	}

	private static final class ClassHandler implements Handler {

		private final JavaIndex index;

		private final JavadocLinkResolver linkResolver;

		private ClassHandler(JavaIndex index, JavadocLinkResolver linkResolver) {
			this.index = index;
			this.linkResolver = linkResolver;
		}

		public String handle(Map<String, Object> attributes) {
			String className = (String) attributes.get("name");
			ClassDescriptor classDescriptor = index.get(className);
			return this.linkResolver.resolve(classDescriptor) + "[`"
					+ className.substring(className.lastIndexOf(".") + 1) + "`]";
		}

	}

	private static class MethodHandler implements Handler {

		private final JavaIndex index;

		private final JavadocLinkResolver linkResolver;

		private MethodHandler(JavaIndex index, JavadocLinkResolver linkResolver) {
			this.index = index;
			this.linkResolver = linkResolver;
		}

		public String handle(Map<String, Object> attributes) {
			String methodName = (String) attributes.get("name");
			String className = (String)attributes.get("class");
			String args = (String)attributes.get("args");
			boolean varargs = attributes.containsValue("varargs") || isVarargs(args);
			ClassDescriptor classDescriptor = index.get(className);
			MethodDescriptor method = args == null ? classDescriptor.getMethod(methodName) : classDescriptor.getMethod(methodName, asArgumentTypes(args));
			if (method.isDeprecated() && !attributes.containsValue("deprecated")) {
				throw new IllegalStateException(className + "." + methodName + " is deprecated. Refer to a different method or add the deprecated attribute.");
			}
			return this.linkResolver.resolve(classDescriptor, method, varargs) + "[`" + method.getName() + "`]";
		}

		private boolean isVarargs(String args) {
			return args != null && args.endsWith(UNICODE_ELLIPSIS + UNICODE_NON_BREAKING_SPACE);
		}

		private List<String> asArgumentTypes(String args) {
			List<String> argumentTypes = new ArrayList<String>();
			for (String arg: args.split(",")) {
				argumentTypes.add(arg.replace(UNICODE_ELLIPSIS + UNICODE_NON_BREAKING_SPACE, "[]").replace("...", "[]"));
			}
			return argumentTypes;
		}

	}

}
