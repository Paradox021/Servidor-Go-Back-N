import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.File;


import java.lang.Short;



public class ro1920send {
	public static void main(String args[]) {
		//ro1920send input_file dest_IP dest_port emulator_IP emulator_port
		int ult = 0;
		int Ack = 0;
		int window = 3;
		int time = 100;
		
		String inputfile = args[0];
		short portdest = Short.valueOf(args[2]);
		String ipemu = args[3];
		int portemu = Integer.parseInt(args[4]);
		if(args.length==6)
		{
			window = Integer.parseInt(args[5]);
		}
		FileInputStream fileInputStream = null;
		File file = new File(inputfile);
		byte[] fileArray = new byte[(int) file.length()];

		try {
			// paso el fichero a un array de bytes
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(fileArray);
			fileInputStream.close();

			

		} catch (Exception e) {
			
		}
		
		try {
			InetAddress dirip = InetAddress.getByName(ipemu);
			InetAddress ipdest=InetAddress.getByName(args[1]);
			DatagramSocket socket = new DatagramSocket();
			byte[] M = fileArray;
			byte[] port = new byte [2];
			byte[] num = new byte [4];
			byte[] fin = new byte[4];
			byte[] cab = new byte[6];
			byte[] ip = new byte[4];
			port[1]= (byte) (portdest);
			port[0]= (byte) ((portdest >> 8) & 0xff);
			
			ip = ipdest.getAddress();
			
			

			// junto cabecera
			System.arraycopy(ip, 0, cab, 0, ip.length);
			System.arraycopy(port, 0, cab, ip.length, port.length);

			//calculo de num de paquetes
			int nump = (int) Math.ceil( (double) M.length / 1454);
			fin = inttobytes(nump);
			int numtampf=(M.length-((nump-1)*1454));
			byte[] tampf= inttobytes(numtampf);
			
			
			System.out.println("Numero de paquetes: " + nump);

			DatagramSocket toReceiver = new DatagramSocket();
			
			boolean cond = true;
			while(cond){
				
				// Sending loop
				while(((ult - Ack) < window) && ult < nump){
					
					
					num = inttobytes(ult+1);
					byte[] fichero = new byte[1472];
					
					
					System.arraycopy(cab, 0, fichero, 0, cab.length);
					System.arraycopy(num, 0, fichero, cab.length, num.length);
					
					System.arraycopy(fin, 0, fichero, cab.length+num.length, num.length);
					System.arraycopy(tampf, 0, fichero, cab.length+num.length+fin.length, tampf.length);

					System.arraycopy(Arrays.copyOfRange(M, ult*1454, ult*1454 + 1454), 0, fichero, cab.length+num.length+fin.length+tampf.length, 1454);
					
					
					

	
					



					// Creacion del paquete
					DatagramPacket packet = new DatagramPacket(fichero, fichero.length, dirip, portemu );

					System.out.println("Enviando paquete numero " + (ult+1));
					
					toReceiver.send(packet);
					

					
					ult++;

				} // fin de bucle while
			
				byte[] ackBytes = new byte[10];
				
				
				DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length);
				
				try{

					
					toReceiver.setSoTimeout(time); //tiempo para reenviar paquete
					
					// Recibir el paquete
					toReceiver.receive(ack);
					byte[] recb = new byte[10];
					recb = ack.getData();
					byte[] numrec = new byte [4];
					System.arraycopy(recb, cab.length, numrec, 0, numrec.length);
					
					int numack = bytestoint(numrec);
					
					System.out.println("Ack del paquete " + numack+" recibido");
					
					// comprueba si es el ultimo paquete
					if(numack == nump){
						System.out.println("paquete enviado");
						cond =false;
					}
					
					Ack = Math.max(Ack, numack);
					
				}catch(SocketTimeoutException e){
					// reenviar paquetes no confirmados
					
					for(int i = Ack; i < ult; i++){
						
						byte[] numm = inttobytes(i+1);
						
						byte[] ficheror = new byte[1472];
						System.arraycopy(cab, 0, ficheror, 0, cab.length);
						System.arraycopy(numm, 0, ficheror, cab.length, numm.length);
						System.arraycopy(fin, 0, ficheror, cab.length+numm.length, numm.length);
						System.arraycopy(tampf, 0, ficheror, cab.length+numm.length+fin.length, tampf.length);
						System.arraycopy(Arrays.copyOfRange(M, i*1454, i*1454 + 1454), 0, ficheror, cab.length+numm.length+fin.length+tampf.length, 1454);
						
						

						
						DatagramPacket packet = new DatagramPacket(ficheror, ficheror.length, dirip, portemu );
						
						
						
						toReceiver.send(packet);
						

						System.out.println("Reenviando paquete numero: " + (i+1));
						
					}
				}
				
			
			
			
			
		    socket.close();
		}} catch (IOException e) {
			
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
