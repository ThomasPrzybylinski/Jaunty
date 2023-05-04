package task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import formula.BoolFormula;
import formula.Conjunctions;
import formula.Constant;
import formula.Disjunctions;
import formula.Literal;
import formula.RITNode;
import formula.Variable;

public class RITCreator {
	public static BoolFormula RITFormula(BoolFormula threeSat) {
		SortedMap<Literal,Integer> sm = threeSat.getFreq(false);
		return RITFormula(threeSat,sm);
	}
	
	public static BoolFormula RITFormula(BoolFormula threeSat, SortedMap<Literal,Integer> sm) {
		threeSat = threeSat.reduce();
		Conjunctions c = new Conjunctions();

		Set<Variable> vars = threeSat.getVars();

		/*for(Entry<Literal,Integer> v : sm.entrySet()) {
			System.out.println(v.getKey() + " = " + v.getValue());
		}*/
		BoolFormula red = threeSat;//.reduce();

		if(red instanceof Constant) {
			return red;
		}

		Literal toSubst;

		if(sm.isEmpty()) {
			toSubst = vars.iterator().next().getPosLit();
		} else {
			//toSubst = vars.iterator().next();
			toSubst = sm.firstKey(); 
		}
			
		SortedMap<Literal,Integer> send = new TreeMap<Literal,Integer>();
		
		send.putAll(sm);
		send.remove(toSubst);

		BoolFormula form1 = threeSat.subst(toSubst,false);
		Disjunctions d = new Disjunctions();
		d.add(toSubst);
		d.add(RITFormula(form1,send));
		c.add(d);

		BoolFormula form2 = threeSat.subst(toSubst,true);
		d = new Disjunctions();
		d.add(toSubst.negate());
		d.add(RITFormula(form2,send));
		c.add(d);

		Disjunctions form3 = new Disjunctions();
		form3.add(form1);
		form3.add(form2);
		c.add(RITFormula(form3,send));

		return c;
	}
	
	public static BoolFormula RITImplicantFormula(BoolFormula threeSat) {
		SortedMap<Literal,Integer> sm = threeSat.getFreq(false);
		return RITImplicantFormula(threeSat,sm);
	}
	
	public static BoolFormula RITImplicantFormula(BoolFormula threeSat, SortedMap<Literal,Integer> sm) {
		//threeSat = threeSat.reduce();
		Disjunctions c = new Disjunctions();

		Set<Variable> vars = threeSat.getVars();

		/*for(Entry<Literal,Integer> v : sm.entrySet()) {
			System.out.println(v.getKey() + " = " + v.getValue());
		}*/
		BoolFormula red = threeSat;//.reduce();

		if(red instanceof Constant) {
			return red;
		}

		Literal toSubst;

		if(sm.isEmpty()) {
			Iterator<Variable> i = vars.iterator();
			if(!i.hasNext()) {
				red = threeSat.reduce();
				return red;
			} else {
				toSubst = vars.iterator().next().getPosLit();
			}
		} else {
			//toSubst = vars.iterator().next();
			toSubst = sm.firstKey(); 
		}
			
		SortedMap<Literal,Integer> send = new TreeMap<Literal,Integer>();
		
		send.putAll(sm);
		send.remove(toSubst);

		BoolFormula form1 = threeSat.subst(toSubst,false);
		Conjunctions d = new Conjunctions();
		d.add(toSubst.negate());
		d.add(RITImplicantFormula(form1,send));
		c.add(d);

		BoolFormula form2 = threeSat.subst(toSubst,true);
		d = new Conjunctions();
		d.add(toSubst);
		d.add(RITImplicantFormula(form2,send));
		c.add(d);

		/*Conjunctions form3 = new Conjunctions();
		form3.add(form1);
		form3.add(form2);
		c.add(RITImplicantFormula(form3,send));*/

		return c;
	}
	
	
	public static RITNode SATFormula(BoolFormula threeSat) {
		RITNode rit = new RITNode();
		SortedMap<Literal,Integer> sm = threeSat.getFreq(false);
		rit.addNodes(SATFormula(threeSat,sm));
		return rit;
	}
	
	public static RITNode[] SATFormula(BoolFormula threeSat, SortedMap<Literal,Integer> sm) {
//		threeSat = threeSat;//.reduce();
		ArrayList<RITNode> nodeList = new ArrayList<RITNode>();

		Set<Variable> vars = threeSat.getVars();

		/*for(Entry<Literal,Integer> v : sm.entrySet()) {
			System.out.println(v.getKey() + " = " + v.getValue());
		}*/
		BoolFormula red = threeSat.reduce();

		if(red instanceof Constant) {
			if(red == Constant.TRUE) {
				return new RITNode[]{};
			} else {
				return null;
			}
		}

		Literal toSubst;

		if(sm.isEmpty()) {
			toSubst = vars.iterator().next().getPosLit();
		} else {
			//toSubst = vars.iterator().next();
			toSubst = sm.firstKey(); 
		}
			
		SortedMap<Literal,Integer> send = new TreeMap<Literal,Integer>();
		
		send.putAll(sm);
		send.remove(toSubst);

		BoolFormula form1 = threeSat.subst(toSubst,true);
		RITNode[] nodes = SATFormula(form1,send);
		
		if(nodes != null) {
			RITNode varNode = new RITNode();
			varNode.setLit(toSubst);
			varNode.addNodes(nodes);
			nodeList.add(varNode);
		}
		

		BoolFormula form2 = threeSat.subst(toSubst,false);
		nodes = SATFormula(form2,send);
		
		if(nodes != null) {
			RITNode varNode = new RITNode();
			varNode.setLit(toSubst.negate());
			varNode.addNodes(nodes);
			nodeList.add(varNode);
		}

		/*Disjunctions form3 = new Disjunctions();
		form3.add(form1);
		form3.add(form2);
		nodes = SATFormula(form3,send);
		
		if(nodes != null) {
			for(RITNode node : nodes) {
				nodeList.add(node);
			}
		}*/

		return nodeList.toArray(new RITNode[nodeList.size()]);
	}
}
