import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.lang.Short;
import java.io.FileOutputStream;


public class ro1920recv {
	//ro1920recv output_file listen_port

	public static void main(String[] args) throws Exception{
		String ofile = args[0];
		short lport = Short.valueOf(args[1]);
		
		
		DatagramSocket fromSender = new DatagramSocket(lport);
		
		
		byte[] receivedData = new byte[1472];
		
		int waitingFor = 0;
		
		
		ArrayList<byte[]> recib = new ArrayList<byte[]>();
		boolean end = false;
		
		while(!end){
			
			System.out.println("Esperando paquete");
			
			// Recibir paquete
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			fromSender.receive(receivedPacket);
			byte[] fichero = receivedPacket.getData();
			
			byte[] cab = new byte[6];
			byte[] num = new byte[4];
			byte[] fin = new byte[4];
			byte[] tampf = new byte[4];
			byte[] packet = new byte[1454];
			
			
			System.arraycopy(fichero, 0, cab, 0, cab.length);
			System.arraycopy(fichero, cab.length, num, 0, num.length);

			System.arraycopy(fichero, cab.length+num.length, fin, 0,fin.length);
			System.arraycopy(fichero, cab.length+num.length+fin.length, tampf, 0, tampf.length);
			System.arraycopy(fichero, cab.length+num.length+fin.length+tampf.length, packet, 0,packet.length);
			
			int numpaq = bytestoint(num);
			int numfin = bytestoint(fin);
			int numtampf = bytestoint(tampf);
			System.out.println("Paquete numero " + numpaq + " recibido");
			
		    	if(numpaq < waitingFor+1)
			{
				byte[] ackBytes = new byte[10];
				byte[] ultpr = new byte[4];
				ultpr = inttobytes(waitingFor);
				System.arraycopy(cab, 0, ackBytes, 0, cab.length);
				System.arraycopy(ultpr, 0, ackBytes, cab.length, ultpr.length);
			
			
			
				DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			
		
			
				fromSender.send(ackPacket);
			
				System.out.println("Paquete descartado (ya se recibío anteriormente)");
				System.out.println("Enviando ack del paquete " + waitingFor);

				
			}
			
			if(numpaq == waitingFor+1){
				waitingFor++;
				recib.add(packet);
				System.out.println("Paquete "+ waitingFor + " guardado");
				
				byte[] ackBytes = new byte[10];
			
				System.arraycopy(cab, 0, ackBytes, 0, cab.length);
				System.arraycopy(num, 0, ackBytes, cab.length, num.length);
			
			
			
				DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length, receivedPacket.getAddress(), receivedPacket.getPort());
			
		
			
				fromSender.send(ackPacket);
			
			
				System.out.println("Enviando ack del paquete " + waitingFor);
			
				if(numpaq==numfin){
					byte[] fileArray = new byte[((numfin-1)*recib.get(0).length)+numtampf] ;
					for(int i=0; i<recib.size();i++)
					{
						if (i==(recib.size()-1))
						{
							System.arraycopy(recib.get(i), 0, fileArray, i*recib.get(i).length, numtampf);
	
						}
						else{

							System.arraycopy(recib.get(i), 0, fileArray, i*recib.get(i).length, recib.get(i).length);
						}
					}
				
				
					System.out.println("ultimo paquete recibido");
					try {
						FileOutputStream fileOuputStream = new FileOutputStream(ofile);
						fileOuputStream.write(fileArray);
						fileOuputStream.close();
		
					} catch (Exception e) {
					
					}
					end = true;
				
				}
			}else if(numpaq > waitingFor+1){
				System.out.println("Paquete descartado (faltan partes anteriores)");
			}
			
			
			

		}
		
		
		
	}
	
	
	private static int bytestoint(byte[] data) {
	    if (data == null || data.length != 4) return 0x0;
	    
	    return (int)( 
	            (0xff & data[0]) << 24  |
	            (0xff & data[1]) << 16  |
	            (0xff & data[2]) << 8   |
	            (0xff & data[3]) << 0
	            );
	}
	
	private static byte[] inttobytes(int data) {
	    return new byte[] {
	        (byte)((data >> 24) & 0xff),
	        (byte)((data >> 16) & 0xff),
	        (byte)((data >> 8) & 0xff),
	        (byte)((data >> 0) & 0xff),
	    };
	}
}
