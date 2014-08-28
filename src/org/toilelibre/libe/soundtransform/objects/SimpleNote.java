package org.toilelibre.libe.soundtransform.objects;

import org.toilelibre.libe.soundtransform.transforms.PitchSoundTransformation;
import org.toilelibre.libe.soundtransform.transforms.SlowdownSoundTransformation;
import org.toilelibre.libe.soundtransform.transforms.SpeedUpSoundTransformation;

public class SimpleNote implements Note {

	private Sound []	attack;
	private Sound []	decay;
	private Sound []	sustain;
	private Sound []	release;
	private int	     frequency;

	public SimpleNote (Sound [] channels, int frequency, int attack, int decay, int sustain, int release) {
		this.frequency = frequency;
		this.attack = new Sound [channels.length];
		this.decay = new Sound [channels.length];
		this.sustain = new Sound [channels.length];
		this.release = new Sound [channels.length];
		for (int i = 0; i < channels.length; i++) {
			this.attack [i] = channels [i].toSubSound (attack, decay);
			this.decay [i] = channels [i].toSubSound (decay, sustain);
			this.sustain [i] = channels [i].toSubSound (sustain, release);
			this.release [i] = channels [i].toSubSound (release, channels [i].getSamples ().length - 1);
		}
	}

	private float getRatio (Sound [] subsound) {
		return subsound [0].getSamples ().length * 1.0f
		        / (this.attack [0].getSamples ().length + this.decay [0].getSamples ().length + this.sustain [0].getSamples ().length + this.release [0].getSamples ().length);
	}

	@Override
	public Sound getAttack (int frequency, int channelnum, int length) {
		return this.transformSubsound (this.attack, channelnum, frequency, (int) (this.getRatio (this.attack) * length));
	}

	@Override
	public Sound getDecay (int frequency, int channelnum, int length) {
		return this.transformSubsound (this.decay, channelnum, frequency, (int) (this.getRatio (this.decay) * length));
	}

	@Override
	public Sound getSustain (int frequency, int channelnum, int length) {
		return this.transformSubsound (this.sustain, channelnum, frequency, (int) (this.getRatio (this.sustain) * length));
	}

	@Override
	public Sound getRelease (int frequency, int channelnum, int length) {
		return this.transformSubsound (this.release, channelnum, frequency, (int) (this.getRatio (this.release) * length));
	}

	@Override
	public int getFrequency () {
		return this.frequency;
	}

	private Sound transformSubsound (Sound [] subSound, int channelNum, int frequency, int length) {

		int percent = (int) (frequency * 100.0 / this.frequency);
		Sound result = subSound [channelNum];

		PitchSoundTransformation pitcher = new PitchSoundTransformation (percent);
		if (percent < 98 || percent > 102) {
			result = pitcher.transform (result);
		}
		double factor = length / subSound.length;
		if (factor < 0.98 || factor > 1.02) {
			if (factor < 0.98) {
				SpeedUpSoundTransformation speedup = new SpeedUpSoundTransformation (100, percent);
				result = speedup.transform (result);

			} else if (factor > 1.02) {
				SlowdownSoundTransformation slowdown = new SlowdownSoundTransformation (100, percent);
				result = slowdown.transform (result);
			}
		}
		return result;
	}

}
