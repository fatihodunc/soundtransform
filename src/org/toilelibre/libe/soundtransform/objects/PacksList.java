package org.toilelibre.libe.soundtransform.objects;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.toilelibre.libe.soundtransform.AudioFileHelper;
import org.toilelibre.libe.soundtransform.Sound2Note;
import org.toilelibre.libe.soundtransform.TransformSound;

public class PacksList {

	private static PacksList packsList = new PacksList (); 
	
	public static PacksList getInstance (){
		return PacksList.packsList;
	}
	
	private PacksList (){
		
	}
	
	private ClassLoader	classLoader	= Thread.currentThread ().getContextClassLoader ();
	TransformSound	    ts	        = new TransformSound ();
	public Pack         defaultPack	= new Pack () {
		                                /**
		 * 
		 */
		                                private static final long	serialVersionUID	= 4439888876778013496L;

		                                {
			                                this.put ("piano", new Range () {
				                                /**
			 * 
			 */
				                                private static final long	serialVersionUID	= 5300824836424234508L;

				                                {
					                                PacksList.this.addNote (this, "piano_a.wav");
				                                }
			                                });

		                                }
	                                };

	private void addNote (Range range, String fileName) {
		try {
			Note n = Sound2Note.convert (ts.fromInputStream (AudioFileHelper.getAudioInputStream (new File (classLoader.getResource (fileName).getFile ()))));
			range.put (n.getFrequency (), n);
		} catch (UnsupportedAudioFileException e) {
		} catch (IOException e) {
		}

	}
}
