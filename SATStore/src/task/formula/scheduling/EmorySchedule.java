package task.formula.scheduling;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import formula.VariableContext;
import formula.simple.CNF;
import task.formula.random.CNFCreator;
import task.formula.scheduling.BasicClassScheduler.ClassInfo;
import task.formula.scheduling.BasicClassScheduler.RoomInfo;
import task.formula.scheduling.BasicClassScheduler.TimeSlot;
import task.translate.ConsoleDecodeable;
import task.translate.FileDecodable;

public class EmorySchedule implements CNFCreator, ConsoleDecodeable
//		FileDecodable
{

	BasicClassScheduler sched;

	public EmorySchedule() {
		TimeSlot[] slots;
		ClassInfo[] classes;
		RoomInfo[] rooms;


		slots = new TimeSlot[] {
				new TimeSlot(new GregorianCalendar(0,0,0,8,0,0), new GregorianCalendar(0,0,0,8,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,9,0,0), new GregorianCalendar(0,0,0,9,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,10,0,0), new GregorianCalendar(0,0,0,10,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,11,0,0), new GregorianCalendar(0,0,0,11,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,12,0,0), new GregorianCalendar(0,0,0,12,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,13,0,0), new GregorianCalendar(0,0,0,13,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,14,0,0), new GregorianCalendar(0,0,0,14,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,15,0,0), new GregorianCalendar(0,0,0,15,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,16,0,0), new GregorianCalendar(0,0,0,16,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,17,0,0), new GregorianCalendar(0,0,0,17,50,0),Calendar.MONDAY,Calendar.WEDNESDAY,Calendar.FRIDAY),

				new TimeSlot(new GregorianCalendar(0,0,0,8,30,0), new GregorianCalendar(0,0,0,9,45,0),Calendar.MONDAY,Calendar.WEDNESDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,10,0,0), new GregorianCalendar(0,0,0,11,15,0),Calendar.MONDAY,Calendar.WEDNESDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,11,30,0), new GregorianCalendar(0,0,0,12,45,0),Calendar.MONDAY,Calendar.WEDNESDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,13,0,0), new GregorianCalendar(0,0,0,14,15,0),Calendar.MONDAY,Calendar.WEDNESDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,14,30,0), new GregorianCalendar(0,0,0,15,45,0),Calendar.MONDAY,Calendar.WEDNESDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,16,0,0), new GregorianCalendar(0,0,0,17,15,0),Calendar.MONDAY,Calendar.WEDNESDAY),

				new TimeSlot(new GregorianCalendar(0,0,0,8,30,0), new GregorianCalendar(0,0,0,9,45,0),Calendar.TUESDAY,Calendar.THURSDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,10,0,0), new GregorianCalendar(0,0,0,11,15,0),Calendar.TUESDAY,Calendar.THURSDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,11,30,0), new GregorianCalendar(0,0,0,12,45,0),Calendar.TUESDAY,Calendar.THURSDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,13,0,0), new GregorianCalendar(0,0,0,14,15,0),Calendar.TUESDAY,Calendar.THURSDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,14,30,0), new GregorianCalendar(0,0,0,15,45,0),Calendar.TUESDAY,Calendar.THURSDAY),
				new TimeSlot(new GregorianCalendar(0,0,0,16,0,0), new GregorianCalendar(0,0,0,17,15,0),Calendar.TUESDAY,Calendar.THURSDAY),
		};


		classes = new ClassInfo[] {
				new ClassInfo("CS 524",16),
				new ClassInfo("CS 540",25),
				new ClassInfo("CS 551",25),
				new ClassInfo("CS 555",16),
				new ClassInfo("CS 556",15),
				new ClassInfo("CS 574",25),

				new ClassInfo("MATH 551",20),
				new ClassInfo("MATH 515",20),
				new ClassInfo("MATH 521",16),
				new ClassInfo("MATH 535",20),
				new ClassInfo("MATH 557",20),
				new ClassInfo("MATH 561",20),
				new ClassInfo("MATH 787R",16),
				new ClassInfo("MATH 788R",16),
				new ClassInfo("MATH 789R",16),

				new ClassInfo("CS 153",40),
				new ClassInfo("CS 170-0",30),
				new ClassInfo("CS 170-1",30),
				new ClassInfo("CS 170-2",30),
				new ClassInfo("CS 170-3",30),
				new ClassInfo("CS 170-4",30),
				new ClassInfo("CS 170-5",30),
				new ClassInfo("CS 171",60),
				new ClassInfo("CS 171Z",16),
				new ClassInfo("CS 190",18),
				new ClassInfo("CS 224",40),
				new ClassInfo("CS 255",32),
				new ClassInfo("CS 323",25),
				new ClassInfo("CS 377",32),
				new ClassInfo("CS 450",25),
				new ClassInfo("CS 456",10),
				new ClassInfo("CS 485-SE",25),
				new ClassInfo("CS 485-SEC",25),

				new ClassInfo("MATH 107-0",28),
				new ClassInfo("MATH 107-1",28),
				new ClassInfo("MATH 107-2",28),
				new ClassInfo("MATH 111-0",29),
				new ClassInfo("MATH 111-1",29),
				new ClassInfo("MATH 111-2",29),
				new ClassInfo("MATH 111-3",29),
				new ClassInfo("MATH 111-4",29),
				new ClassInfo("MATH 111-5",29),
				new ClassInfo("MATH 111-6",29),
				new ClassInfo("MATH 111-7",29),
				new ClassInfo("MATH 111-8",29),
				new ClassInfo("MATH 111-9",29),
				new ClassInfo("MATH 111-10",29),
				new ClassInfo("MATH 111-11",29),
				new ClassInfo("MATH 111-12",29),
				new ClassInfo("MATH 111-13",29),
				new ClassInfo("MATH 111-14",29),
				new ClassInfo("MATH 112-0",28),
				new ClassInfo("MATH 112-1",28),
				new ClassInfo("MATH 112-2",28),
				new ClassInfo("MATH 112-3",28),
				new ClassInfo("MATH 112-4",28),
				new ClassInfo("MATH 112Z-0",28),
				new ClassInfo("MATH 112Z-1",35),
				new ClassInfo("MATH 112Z-2",35),
				new ClassInfo("MATH 115-0",60),
				new ClassInfo("MATH 115-1",60),
				new ClassInfo("MATH 115-2",60),
				new ClassInfo("MATH 116",60),
				new ClassInfo("MATH 211-0",45),
				new ClassInfo("MATH 211-1",45),
				new ClassInfo("MATH 211-2",45),
				new ClassInfo("MATH 211-3",40),
				new ClassInfo("MATH 211-4",40),
				new ClassInfo("MATH 211-5",40),
				new ClassInfo("MATH 212-0",35),
				new ClassInfo("MATH 212-1",35),
				new ClassInfo("MATH 221-0",28),
				new ClassInfo("MATH 221-1",28),
				new ClassInfo("MATH 221-2",28),
				new ClassInfo("MATH 221-3",28),
				new ClassInfo("MATH 221-4",28),
				new ClassInfo("MATH 250-0",16),
				new ClassInfo("MATH 250-1",16),
				new ClassInfo("MATH 250-2",16),
				new ClassInfo("MATH 250-3",16),
				new ClassInfo("MATH 315",25),
				new ClassInfo("MATH 318",25),
				new ClassInfo("MATH 328",25),
				new ClassInfo("MATH 346",25),
				new ClassInfo("MATH 351",25),
				new ClassInfo("MATH 361-0",25),
				new ClassInfo("MATH 361-1",25),
				new ClassInfo("MATH 361-2",25),
				new ClassInfo("MATH 411",25),
				new ClassInfo("MATH 421",25),
				new ClassInfo("MATH 425",25),
		};

		rooms = new RoomInfo[] {
				new RoomInfo("E308",16),  
				new RoomInfo("E308A",25),  
				new RoomInfo("E406",20),  
				new RoomInfo("E408",25),  
//
//				new RoomInfo("PAIS 561",15), 

				new RoomInfo("W201",60),  
				new RoomInfo("W301",32),  
				new RoomInfo("W302",25),  
				new RoomInfo("W303",40),  
				new RoomInfo("W304",30),  
				new RoomInfo("W306",30),  

//				new RoomInfo("White Hall 101",28),
//				new RoomInfo("White Hall 102",40),
				new RoomInfo("White Hall 111",60),
//				new RoomInfo("White Hall 112",45),
		};



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
		return "EmorySchedule";
	}

}
