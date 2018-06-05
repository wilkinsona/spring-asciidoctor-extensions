package io.spring.asciidoctor.java;

import io.spring.asciidoctor.java.JavaIndex.ClassDescriptor;
import io.spring.asciidoctor.java.JavaIndex.MethodDescriptor;

final class JavadocLinkResolver {

	private final String urlRoot;

	JavadocLinkResolver(String urlRoot) {
		this.urlRoot = urlRoot;
	}

	String resolve(ClassDescriptor classDescriptor) {
		return this.urlRoot + classDescriptor.getName().replace(".", "/") + ".html";
	}

	String resolve(ClassDescriptor classDescriptor, MethodDescriptor method, boolean varargs) {
		return resolve(classDescriptor)
				+ "#" + method.getName()
				+ "-" + getArgumentSignature(method, varargs);
	}

	private String getArgumentSignature(MethodDescriptor method, boolean varargs) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < method.getArgumentTypes().size(); i++) {
			String argumentType = method.getArgumentTypes().get(i);
			if (argumentType.endsWith("[]")) {
				builder.append(argumentType.substring(0, argumentType.length() - 2));
				builder.append(varargs ? "...": ":A");
			}
			else {
				builder.append(argumentType);
			}
			builder.append("-");
		}
		if (builder.length() == 0) {
			builder.append("-");
		}
		return builder.toString();
	}

}
