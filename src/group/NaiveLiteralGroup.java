package group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NaiveLiteralGroup extends LiteralGroup {
	private List<LiteralPermutation> generators;
	private LiteralPermutation id = null;
	private int size = -1;
	
	public NaiveLiteralGroup(Collection<LiteralPermutation> generators) {
		this.generators = new ArrayList<LiteralPermutation>(generators.size());
		
		for(LiteralPermutation gen : generators) {
			if(size == -1) { 
				size = gen.size();
			} else {
				if(size != gen.size()) {
					throw new InvalidGroupException("Generators do not act on the same set of literals!");
				}
			}
			if(gen.isId()) id = gen;
			this.generators.add(gen);
		}
		
		if(this.generators.size() == 0) {
			throw new InvalidGroupException("Group must have at least one permutation!");
		}
	}
	
	public NaiveLiteralGroup(LiteralPermutation... generators) {
		this.generators = new ArrayList<LiteralPermutation>(generators.length);
		
		for(LiteralPermutation gen : generators) {
			if(size == -1) { 
				size = gen.size();
			} else {
				if(size != gen.size()) {
					throw new InvalidGroupException("Generators do not act on the same set of literals!");
				}
			}
			if(gen.isId()) id = gen;
			this.generators.add(gen);
		}
		
		if(this.generators.size() == 0) {
			throw new InvalidGroupException("Group must have at least one permutation!");
		}
	}

	@Override
	public List<LiteralPermutation> getGenerators() {
		return Collections.unmodifiableList(generators);
	}

	@Override
	public LiteralPermutation getId() {
		if(id == null) {
			id = new LiteralPermutation(generators.get(0).size());
		}
		
		return id;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public NaiveLiteralGroup getNewInstance(Collection<LiteralPermutation> generators) {
		return new NaiveLiteralGroup(generators);
	}
}
