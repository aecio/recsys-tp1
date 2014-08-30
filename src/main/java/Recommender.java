public class Recommender {
	public static void main(String[] args) {
		if(args.length != 3) {
			throw new IllegalArgumentException("Invalid parameters.");
		}
		String users = args[0];
		String items = args[1];
		String ratings = args[2];
		
		System.out.println(users +" - "+items+" - "+ratings);
	}
}
