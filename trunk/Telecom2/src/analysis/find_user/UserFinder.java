package analysis.find_user;

public class UserFinder {
	public String find(String q) {
		return "";
	}
	
	
	public static void main(String[] args) {
		UserFinder uf = new UserFinder();
		String q = "2014-04-18;hh:mm;2014-04-15;hh:mm;false;((44.051063844894934, 11.828528808593774), (44.05306384489493, 11.830528808593726));2014-04-17;hh:mm;2014-04-11;hh:mm;true;((43.991814500489916, 10.515662597656274), (43.99381450048991, 10.517662597656226));";
		System.out.println(uf.find(q));
	}
}
