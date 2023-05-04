package task.formula.scheduling;

import java.util.Calendar;
import java.util.GregorianCalendar;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;
import task.formula.scheduling.BasicClassScheduler.ClassInfo;
import task.formula.scheduling.BasicClassScheduler.RoomInfo;
import task.formula.scheduling.BasicClassScheduler.TimeSlot;
import task.translate.ConsoleDecodeable;

public class TestSchedule implements CNFCreator, ConsoleDecodeable
//		FileDecodable
		{

	BasicClassScheduler sched;
	
	public TestSchedule() {
		TimeSlot[] slots = new TimeSlot[3];
		ClassInfo[] classes = new ClassInfo[2];
		RoomInfo[] rooms = new RoomInfo[2];
		
		slots[0] = new TimeSlot(new GregorianCalendar(0,0,0,9,0,0), new GregorianCalendar(0,0,0,12,0,0),Calendar.MONDAY);
		slots[1] = new TimeSlot(new GregorianCalendar(0,0,0,11,0,0), new GregorianCalendar(0,0,0,14,0,0),Calendar.MONDAY);
		slots[2] = new TimeSlot(new GregorianCalendar(0,0,0,13,0,0), new GregorianCalendar(0,0,0,16,0,0),Calendar.MONDAY);
		
		classes[0] = new ClassInfo("Math101-000",30);
		
		classes[1] = new ClassInfo("Math102-000",20);
		
//		classes[1] = new ClassInfo("Math101-001",30);
//		classes[2] = new ClassInfo("Math101-002",30);
//		classes[3] = new ClassInfo("Math102-000",20);
//		classes[4] = new ClassInfo("CS100-000",20);
//		classes[5] = new ClassInfo("CS100-001",20);
//		classes[6] = new ClassInfo("CS100-002",20);
//		classes[7] = new ClassInfo("CS200-000",15);
//		classes[8] = new ClassInfo("CS200-001",15);
		
		rooms[0] = new RoomInfo("101",30);
		rooms[1] = new RoomInfo("102",20);
//		rooms[2] = new RoomInfo("103",20);
		
		sched = new BasicClassScheduler(classes,rooms,slots);
	}
	
	@Override
	public CNF generateCNF(VariableContext context) {
		return sched.generateCNF(context);
	}

	
//	@Override
//	public void fileDecoding(File dir, String filePrefix, int[] model)
//			throws IOException {
//		sched.fileDecoding(dir,filePrefix,model);
//
//	}
//
//	@Override
//	public void fileDecoding(String filePrefix, int[] model) throws IOException {
//		sched.fileDecoding(filePrefix,model);
//
//	}

	@Override
	public String consoleDecoding(int[] model) {
		return sched.consoleDecoding(model);
	}

	public String toString() {
		return "TestSchedule";
	}

}
