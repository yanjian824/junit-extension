package org.sdet.junit.extension;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.sdet.junit.extension.annotation.Author;
import org.sdet.junit.extension.annotation.AuthorFilter;
import org.sdet.junit.extension.annotation.Priority;
import org.sdet.junit.extension.annotation.PriorityFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomFilter extends Filter {

	private Set<String> authors;

	private Set<Class<?>> priorities;

	public CustomFilter(AuthorFilter authors, PriorityFilter priorities) {		
		if (authors != null) {
			this.authors = new HashSet<String>(Arrays.asList(authors.value()));
		} else {
			this.authors = null;
		}

		if (priorities != null) {
			this.priorities = new HashSet<Class<?>>(Arrays.asList(priorities.value()));
		}			
		else {
			this.priorities = null;
		}			
	}

	@Override
	public boolean shouldRun(Description description) {
		return shouldfilterByAuthor(description) && shouldfilterByPriority(description);
	}

	private boolean shouldfilterByAuthor(Description description) {
		boolean result = true;
		
		if (description.isSuite())
			return result;
		
		if (authors != null) {
			Author author = description.getAnnotation(Author.class);
			if (author == null) {
				System.err.println(String.format("请为用例[%s]添加Author注解", description.getDisplayName()));
				result = false;
			} else if (!authors.contains(author.value())) {
				result = false;
			}
		}
		
		return result;
	}

	private boolean shouldfilterByPriority(Description description) {
		boolean result = true;
		
		if (description.isSuite())
			return result;
		
		if (priorities != null) {
			Priority priority = description.getAnnotation(Priority.class);
			if (priority == null) {
				System.err.println(String.format("请为用例[%s]添加Priority注解", description.getDisplayName()));
				result = false;
			} else if (!priorities.contains(priority.value())) {
				result = false;
			}
		}
		
		return result;
	}

	@Override
	public String describe() {
		return toString();
	}
}
