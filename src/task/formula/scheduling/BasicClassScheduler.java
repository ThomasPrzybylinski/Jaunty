package task.formula.scheduling;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import task.formula.random.CNFCreator;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;
import formula.VariableContext;
import formula.simple.CNF;




public class BasicClassScheduler implements CNFCreator, ConsoleDecodeable,
FileDecodable {
	public static class ClassInfo {
		protected String className;
		protected int numStudents;
		protected int index;

		public ClassInfo(String className, int numStudents) {
			super();
			this.className = className;
			this.numStudents = numStudents;
		}
	}

	public static class RoomInfo {
		protected String room;
		protected int capacity;
		protected int index;

		public RoomInfo(String room, int capacity) {
			super();
			this.room = room;
			this.capacity = capacity;
		}
	}

	public static class TimeSlot {
		protected int[] days; //From Calendar.
		protected GregorianCalendar startTime;
		protected GregorianCalendar endTime;
		protected List<TimeSlot> overlaps = new LinkedList<TimeSlot>();
		protected int index;

		public TimeSlot(GregorianCalendar start, GregorianCalendar end, int... days) {
			startTime = new GregorianCalendar();
			startTime.clear();
			startTime.set(Calendar.HOUR_OF_DAY,start.get(Calendar.HOUR_OF_DAY));
			startTime.set(Calendar.MINUTE,start.get(Calendar.MINUTE));

			endTime = new GregorianCalendar();
			endTime.clear();
			endTime.set(Calendar.HOUR_OF_DAY,end.get(Calendar.HOUR_OF_DAY));
			endTime.set(Calendar.MINUTE,end.get(Calendar.MINUTE));

			this.days = days;
		}

	}

	private CNF ret;
	ArrayList<Assignment> vars;

	//Right now we assume that every class can take any time slot
	public BasicClassScheduler(ClassInfo[] classes, RoomInfo[] rooms, TimeSlot[] slots) {
		for(int k = 0; k < classes.length; k++) {
			classes[k].index = k;
		}

		for(int k = 0; k < rooms.length; k++) {
			rooms[k].index = k;
		}

		for(int k = 0; k < slots.length; k++) {
			slots[k].index = k;
			slots[k].overlaps.add(slots[k]);

			for(int i = k+1; i < slots.length; i++) {
				if(daysOverlap(slots[k],slots[i])
						&& timesOverlap(slots[k],slots[i])) {
					slots[k].overlaps.add(slots[i]);
					slots[i].overlaps.add(slots[k]);
				}
			}
		}

		ret = new CNF(new VariableContext());
		vars = new ArrayList<Assignment>();

		for(int k = 0; k < classes.length; k++) {
			int curVar = vars.size()+1;
			for(int i = 0; i < rooms.length; i++) {
				if(classes[k].numStudents <= rooms[i].capacity) {
					for(int j = 0; j < slots.length; j++) {
						int var = vars.size()+1;
						vars.add(new Assignment(classes[k],rooms[i],slots[j],var));
					}
				}
			}
			int endVar = vars.size();

			int[] eachClassMustBeAssignedSomewhere = new int[endVar-curVar+1];

			for(int i = curVar; i <= endVar; i++) {
				eachClassMustBeAssignedSomewhere[i-curVar] = i;
			}
			ret.fastAddClause(eachClassMustBeAssignedSomewhere);
		}

		for(int k = 0; k < vars.size(); k++) {
			Assignment a1 = vars.get(k);
			for(int i = k+1; i < vars.size(); i++) {
				Assignment a2 = vars.get(i);
				if(a1.clas == a2.clas || overlap(a1,a2)) {
					ret.fastAddClause(-a1.var,-a2.var);
				}
			}
		}
		
		ret.sort();
	}


	private boolean daysOverlap(TimeSlot timeSlot, TimeSlot timeSlot2) {
		for(int k = 0; k < timeSlot.days.length; k++) {
			for(int i = 0; i < timeSlot2.days.length; i++) {
				if(timeSlot.days[k] == timeSlot2.days[i]) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean timesOverlap(TimeSlot timeSlot, TimeSlot timeSlot2) {
		return timeSlot.startTime.compareTo(timeSlot2.endTime) <= 0 && timeSlot.endTime.compareTo(timeSlot2.startTime) >= 0;
	}

	private class Assignment {
		ClassInfo clas;
		RoomInfo room;
		TimeSlot slot;
		private int var;

		public Assignment(ClassInfo clas, RoomInfo room, TimeSlot slot, int var) {
			super();
			this.clas = clas;
			this.room = room;
			this.slot = slot;
			this.var = var;
		}


	}

	@Override
	public CNF generateCNF(VariableContext context) {
		context.ensureSize(vars.size());
		ret.setContext(context);
		return ret;
	}


	private boolean overlap(Assignment a1, Assignment a2) {
		return a1.room == a2.room &&
				a1.slot.overlaps.contains(a2.slot);
	}


	private class CompAssn implements Comparator<Assignment>{

		@Override
		public int compare(Assignment o1, Assignment o2) {
			int temp = 0;
			if((temp = o1.slot.days[0] - o2.slot.days[0]) != 0) {
				return temp;
			} else if((temp = o1.slot.startTime.compareTo(o2.slot.startTime)) != 0) {
				return temp;
			} else return o1.room.room.compareTo(o2.room.room);
		}

	}

	@Override
	public void fileDecoding(File dir, String filePrefix, int[] model)
			throws IOException {
		ArrayList<Assignment> assns = new ArrayList<Assignment>();

		for(int k = 0; k < model.length; k++) {
			if(model[k] > 0) {
				assns.add(vars.get(k));
			}
		}

		Collections.sort(assns,new CompAssn());
		SimpleDateFormat format = new SimpleDateFormat("hh:mma");

		File f = new File(dir, filePrefix + ".txt");
		PrintWriter pw = new PrintWriter(f);

		for(Assignment a : assns) {
			for(int i : a.slot.days) {
				pw.print(day(i));
			}

			pw.print(" \t");

			pw.print(format.format(a.slot.startTime.getTime()));
			pw.print("-");
			pw.print(a.slot.endTime.getTime());

			pw.print(" \t");
			pw.print(a.room.room);
			pw.print(" \t");
			pw.println(a.clas);
		}

		pw.close();
	}

	private String day(int day) {
		switch(day) {
		case Calendar.MONDAY: return "M";
		case Calendar.TUESDAY: return "T";
		case Calendar.WEDNESDAY: return "W";
		case Calendar.THURSDAY: return "Th";
		case Calendar.FRIDAY: return "F";
		default: return "#";
		}
	}

	@Override
	public void fileDecoding(String filePrefix, int[] model) throws IOException {
		fileDecoding(new File("."),filePrefix,model);
	}

	@Override
	public String consoleDecoding(int[] model) {
		ArrayList<Assignment> assns = new ArrayList<Assignment>();

		for(int k = 0; k < model.length; k++) {
			if(model[k] > 0) {
				assns.add(vars.get(k));
			}
		}

		Collections.sort(assns,new CompAssn());
		SimpleDateFormat format = new SimpleDateFormat("hh:mma");

		StringBuilder sb = new StringBuilder();

		for(Assignment a : assns) {
			for(int i : a.slot.days) {
				sb.append(day(i));
			}

			sb.append(" \t");

			sb.append(format.format(a.slot.startTime.getTime()));
			sb.append("-");
			sb.append(format.format(a.slot.endTime.getTime()));

			sb.append(" \t");
			sb.append(a.room.room);
			sb.append(" \t");
			sb.append(a.clas.className).append(newline);
		}

		return sb.toString();
	}


}
