package utils.kdtree;

public class Test {
	public static void main(String[] args) {
		
		KDTree tree = new KDTree(2);
		tree.insert(new double[]{0,0},"ciao1");
		tree.insert(new double[]{1,1},"ciao2");
		tree.insert(new double[]{2,2},"ciao3");
		tree.insert(new double[]{0.5,1},"ciao4");
		
		String s = (String)tree.nearest(new double[]{0.1,0.2});
		System.out.println(s);
		
	}
}
