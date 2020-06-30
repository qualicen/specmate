package com.specmate.cause_effect_patterns.parse;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.internal.util.SortedIntSet;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Representation for a dependency parse.
 *
 * @author Dominik
 *
 */
public class DependencyParsetree {
	private static Set<String> ignoreDependency;
	static {
		DependencyParsetree.ignoreDependency = new HashSet<String>();
		DependencyParsetree.ignoreDependency.add("punct");
	}

	private static String ROOT = "ROOT";


	private Map<Token, DependencyNode> dependencies;
	private Set<Token> heads;
	private List<TextInterval> treeFragments;
	private SortedIntSet tokenOrder;

	public SortedIntSet getTokenOrder() {
		return tokenOrder;
	}

	public static DependencyParsetree generateFromJCas(JCas jcas) {
		DependencyParsetree result = new DependencyParsetree();

		Collection<Dependency> dependencyList = JCasUtil.select(jcas, Dependency.class);

		for(Dependency d: dependencyList) {
			Token governor = d.getGovernor();

			if(DependencyParsetree.ignoreDependency.contains(d.getDependencyType())) {
				continue;
			}

			if(!result.dependencies.containsKey(governor)) {
				result.dependencies.put(governor, new DependencyNode());
				result.addFragment(governor);
			}

			if(d.getDependencyType().equals(ROOT)) {
				result.heads.add(governor);
			} else {
				result.dependencies.get(governor).addDepenency(d);
				result.addFragment(d.getDependent());
			}
		}
		result.minimizeTreeFragments();
		return result;
	}

	public static DependencyParsetree getSubtree(DependencyParsetree data, Token token) {
		DependencyParsetree result = new DependencyParsetree(data.tokenOrder, token);

		if(data.hasDependencies(token)) {
			for (Dependency d: data.getDependencyNode(token)) {
				Token child = d.getDependent();
				if(child == token) {
					continue;
				}
				result.addSubtree(DependencyParsetree.getSubtree(data, child), d);
			}
		}
		return result;
	}

	public DependencyParsetree(SortedIntSet order) {
		this();
		tokenOrder = order;
	}

	public DependencyParsetree(SortedIntSet sortedIntSet, Token head) {
		this(sortedIntSet);
		heads.add(head);
		addFragment(head);
	}

	public DependencyParsetree() {
		dependencies = new HashMap<Token, DependencyNode>();
		heads = new HashSet<Token>();
		treeFragments = new Vector<DependencyParsetree.TextInterval>();
		tokenOrder = new SortedIntSet();
	}

	public void addSubtree(DependencyParsetree subtree) {
		for(Token token: subtree.dependencies.keySet()) {
			if(dependencies.containsKey(token)) {
				DependencyNode nodeA = dependencies.get(token);
				DependencyNode nodeB = subtree.dependencies.get(token);
				nodeA.addDependencies(nodeB);
			} else {
				dependencies.put(token, subtree.dependencies.get(token));
			}
		}
		treeFragments.addAll(subtree.treeFragments);
		tokenOrder.union(subtree.tokenOrder);
		minimizeTreeFragments();
	}

	public void addSubtree(DependencyParsetree subtree, Dependency dependency) {
		addDependency(dependency);
		this.addSubtree(subtree);
	}

	public DependencyNode getDependencyNode(Token dependent) {
		return dependencies.get(dependent);
	}

	public Collection<Token> getHeads() {
		return heads;
	}

	public boolean hasDependencies(Token token) {
		return dependencies.containsKey(token);
	}

	private void addFragment(Token token) {
		treeFragments.add(new TextInterval(token));
		tokenOrder.add(token.getBegin());
	}

	/**
	 * Merges tree fragments if they can be merged to a bigger fragment.
	 */
	private void minimizeTreeFragments() {
		for(int i=0; i< treeFragments.size(); i++) {
			TextInterval iInt = treeFragments.get(i);

			for(int j = i+1; j<treeFragments.size(); j++) {
				TextInterval comb = iInt.combine(treeFragments.get(j), tokenOrder);
				if(comb != null) {
					treeFragments.remove(j);
					treeFragments.set(i, comb);
					i = -1;
					break;
				}
			}
		}

		Collections.sort(treeFragments);
	}

	public int getTextIntervallCount() {
		return treeFragments.size();
	}

	public TextInterval getTextInterval(int index) {
		return treeFragments.get(index);
	}

	public String getRepresentationString(boolean capitalize) {
		String result = treeFragments.stream()
				.map(f -> f.text)
				.filter(DependencyParsetree::filterCondition)
				.collect(Collectors.joining(" "));
		if(capitalize) {
			result = result.substring(0, 1).toUpperCase() + result.substring(1);
		}

		return result;
	}

	/**
	 * Filters punctuation without filtering operators
	 * @param str
	 * @return
	 */
	private static boolean filterCondition(String str) {
		if(str.length() > 1) {
			return true;
		}
		// Special Exception for operators.
		return str.matches("[<>=]");

	}

	public String getTreeFragmentText() {
		String result = "Fragments:\n";
		for(TextInterval i: treeFragments) {
			result += "\t"+i+"\n";
		}
		return result;
	}

	@Override
	public String toString() {
		String result = "Roots:\n";
		for(Token root: heads) {
			result+= "\t"+root.getCoveredText()+"\n";
		}

		result+="Dependencies:\n";
		for(Token t: dependencies.keySet()) {
			result+= "\t"+t.getCoveredText()+"\n";
			DependencyNode node = getDependencyNode(t);
			for(String key: node.getKeySet()) {
				List<Dependency>dependencies = node.getDependenciesFromTag(key);
				for(Dependency d: dependencies) {
					result += "\t\t--"+d.getDependencyType()+"-->"+d.getDependent().getCoveredText()+"\n";
				}
			}
		}

		result+= getTreeFragmentText();
		return result;
	}

	public void addDependency(Dependency dependency) {
		Token governor = dependency.getGovernor();
		if(!dependencies.containsKey(governor)) {
			dependencies.put(governor, new DependencyNode());
			addFragment(governor);
		}
		dependencies.get(governor).addDepenency(dependency);
	}

	public class TextInterval implements Comparable<TextInterval>{
		public int from, to;
		public String text;

		public TextInterval(int from, int to, String text) {
			this.from = from;
			this.to = to;
			this.text = text;
		}

		public TextInterval(Token token) {
			this(token.getBegin(), token.getEnd(), token.getCoveredText());
		}

		public TextInterval combine(TextInterval other, SortedIntSet order) {
			TextInterval begin = this;
			TextInterval end = other;

			if(other.from < from || (other.from == from && other.to >= to)) {
				begin = other;
				end = this;
			}

			if(begin.to == end.from) {
				return new TextInterval(begin.from, end.to, begin.text+end.text);
			}

			if(begin.getLastIndex(order) == end.getFirstIndex(order)-1) {
				return new TextInterval(begin.from, end.to, begin.text+" "+end.text);
			}

			if(begin.to >= end.to) {
				return begin;
			}

			return null;
		}

		private int getFirstIndex(SortedIntSet order) {
			return order.find(from);
		}

		private int getLastIndex(SortedIntSet order) {
			int i = getFirstIndex(order);
			while(i < order.size()) {
				if(to < order.get(i)) {
					break;
				}
				i++;
			}
			return i-1;
		}

		@Override
		public String toString() {
			return text + " " + from+ " - "+to;
		}

		@Override
		public int compareTo(TextInterval o) {
			if(to <= o.from) {
				return -1;
			}
			if(o.to <= from) {
				return 1;
			}
			return 0;
		}
	}

}
