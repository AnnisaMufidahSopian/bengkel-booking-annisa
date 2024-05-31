package com.bengkel.booking.services;

import java.util.Scanner;

public class Validation {

	private static Scanner input = new Scanner(System.in);

	public static String validasiInput(String question, String errorMessage, String regex) {
		String result;
		boolean isLooping = true;
		do {
			System.out.print(question);
			result = input.nextLine();

			// validasi menggunakan matches
			if (result.matches(regex)) {
				isLooping = false;
			} else {
				System.out.println(errorMessage);
			}

		} while (isLooping);

		return result;
	}

	public static int validasiNumberWithRange(String question, String errorMessage, String regex, int max, int min) {
		int result;
		boolean isLooping = true;
		do {
			result = Integer.valueOf(validasiInput(question, errorMessage, regex));
			if (result >= min && result <= max) {
				isLooping = false;
			} else {
				System.out.println("Pilihan angka " + min + " s.d " + max);
			}
		} while (isLooping);

		return result;
	}
}
