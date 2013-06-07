package de.tum.ascodt.plugin;

public enum TestEnum {
	A(0),
	B(1);
	int value;
	TestEnum(int v){
		this.value=v;
	}
	public int getValue(){
		return value;
	}
}
class Tester{
	void testMethod(TestEnum e){
		
	}
	void testMethod(int e){
		
	}
	void test(){
		testMethod(1);
		
	}
}