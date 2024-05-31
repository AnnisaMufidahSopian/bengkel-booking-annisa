package com.bengkel.booking.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.bengkel.booking.models.BookingOrder;
import com.bengkel.booking.models.Customer;
import com.bengkel.booking.models.ItemService;
import com.bengkel.booking.models.MemberCustomer;
import com.bengkel.booking.models.Vehicle;

public class BengkelService {
	private static final Scanner input = new Scanner(System.in);
	// Silahkan tambahkan fitur-fitur utama aplikasi disini

	// Login
	public static String Login(List<Customer> listAllCustomers) {
		System.out.println("Login");
		System.out.println("==================");
		System.out.print("Masukan id customer: ");
		String customerId = input.nextLine();
		Customer customer = listAllCustomers.stream()
				.filter(cust -> cust instanceof Customer)
				.map(cust -> (Customer) cust)
				.filter(cust -> cust.getCustomerId().equals(customerId))
				.findFirst()
				.orElse(null);

		if (customer == null) {
			System.err.println("\nId customer tidak ditemukan\n");
			return null;
		}

		System.out.print("Masukan password : ");
		String password = input.nextLine();
		Customer pass = listAllCustomers.stream()
				.filter(cust -> cust.getPassword().equals(password))
				.findFirst()
				.orElse(null);

		if (pass == null) {
			System.err.println("Password salah");
			return null;
		}
		return customer.getCustomerId();
	}

	// Info Customer
	public static void CustomerInfo(List<Customer> listAllCustomers, String id) {
		Customer customer = listAllCustomers.stream()
				.filter(cust -> cust instanceof Customer)
				.map(cust -> (Customer) cust)
				.filter(cust -> cust.getCustomerId().equals(id))
				.findFirst()
				.orElse(null);

		if (customer == null) {
			System.err.println("\nCustomer tidak ditemukan\n");
		}
		MemberCustomer memberCustomer = listAllCustomers.stream()
				.filter(cust -> cust instanceof MemberCustomer)
				.map(cust -> (MemberCustomer) cust)
				.filter(member -> member.getCustomerId().equals(customer.getCustomerId()))
				.findFirst()
				.orElse(null);

		System.out.println("Customer Id : " + customer.getCustomerId());
		System.out.println("Nama : " + customer.getName());
		System.out.println("Alamat : " + customer.getAddress());

		if (memberCustomer != null) {
			System.out.println("Customer Status : Member");
			System.out.println("Saldo Koin : " + memberCustomer.getSaldoCoin());
		} else {
			System.out.println("Customer Status : Non Member");
		}

		System.out.println("List Kendaraan : ");
		PrintService.printVechicle(customer.getVehicles());
	}

	// Booking atau Reservation
	public static void Booking(List<Customer> listAllCustomers, List<ItemService> listAllItemService,
			List<BookingOrder> bookingOrders, String id) {
		Customer customer = findCustomer(listAllCustomers, id);
		if (customer == null) {
			System.err.println("Customer tidak ada");
			return;
		}

		List<Vehicle> vehicles = customer.getVehicles();
		System.out.println("Kendaraan yang dimiliki : ");
		vehicles.stream()
				.forEach(vehicle -> {
					System.out.println("tipe kendaraan : " + vehicle.getVehicleType() + ", Nomor Kendaraan : "
							+ vehicle.getVehiclesId());
				});

		System.out.println("Masukkan vehicle id : ");
		String vehicleId = input.nextLine();

		Vehicle selectedVehicle = vehicles.stream()
				.filter(vehicle -> vehicle.getVehiclesId().equals(vehicleId))
				.findFirst()
				.orElse(null);

		if (selectedVehicle == null) {
			System.err.println("Bukan kendaraan anda");
			return;
		}
		System.out.println("\nService yang tersedia untuk " + selectedVehicle.getVehicleType());

		listAllItemService.stream()
				.filter(item -> item.getVehicleType().equals(selectedVehicle.getVehicleType()))
				.forEach(service -> {
					System.out.println("Service Id : " + service.getServiceId() + ", Service Name : "
							+ service.getServiceName() + " Price : " + service.getPrice());
				});

		List<ItemService> selectedService = new ArrayList<>();
		boolean addService = true;
		while (addService) {
			System.out.println("Masukkan id services : ");
			String serviceId = input.nextLine();

			ItemService service = listAllItemService.stream()
					.filter(item -> item.getServiceId().equals(serviceId))
					.findFirst()
					.orElse(null);

			if (service == null) {
				System.err.println("Service tidak ditemukan");
				continue;
			}

			selectedService.add(service);

			System.out.println("Apakah anda ingin menambahkan service lainnya? (Y/T) : ");
			String confirm = input.nextLine();

			if (confirm.equalsIgnoreCase("Y")) {
				addService = true;
			} else if (confirm.equalsIgnoreCase("T")) {
				addService = false;
			} else {
				System.err.println("Input tidak valid, masukkan Y atau T");
				continue;
			}
		}

		double totalPayment = selectedService.stream()
				.mapToDouble(ItemService::getPrice).sum();
		System.out.println("Total biaya service : " + totalPayment);

		System.out.println("Pilih metode pembayaran (Saldo coin atau cash)");
		System.out.println("1. Saldo Coin");
		System.out.println("2. Cash");
		int choice = Validation.validasiNumberWithRange("Pilih metode pembayaran : ", "Input tidak valid", "^[1-2]$", 2,
				1);

		String paymentMethod = "";
		switch (choice) {
			case 1:
				if (customer instanceof MemberCustomer) {
					MemberCustomer member = (MemberCustomer) customer;
					if (member.getSaldoCoin() >= totalPayment) {
						paymentMethod = "Saldo coin";
					} else {
						System.out.println("Saldo coin tidak cukup");
						return;
					}
				} else {
					System.out.println("Anda bukan member, saldo coin hanya dapat digunakan oleh member");
					return;
				}
				break;
			case 2:
				paymentMethod = "Cash";
				break;
			default:
				break;
		}
		BookingOrder booking = BookingOrder.builder()
				.bookingId("Book-Cust-00" + (bookingOrders.size() + 1) + "-" + id)
				.customer(customer)
				.services(selectedService)
				.paymentMethod(paymentMethod)
				.totalServicePrice(totalPayment)
				.totalPayment(0)
				.build();

		booking.calculatePayment();
		bookingOrders.add(booking);
		System.out.println("Booking Berhasil !!");
		System.out.println("Total Harga Service : " + totalPayment);
		System.out.println("Total Pembayaran : " + booking.getTotalPayment());
	}

	// Top Up Saldo Coin Untuk Member Customer
	public static void TopUpSaldoCoin(List<Customer> listAllCustomers, String id) {
		Customer customer = findCustomer(listAllCustomers, id);
		if (customer == null) {
			System.err.println("Customer tidak ada");
			return;
		}
		if (!(customer instanceof MemberCustomer)) {
			System.err.println("Maaf fitur ini hanya untuk member saja!");
			return;
		}

		MemberCustomer member = (MemberCustomer) customer;
		String input = Validation.validasiInput("Masukkan jumlah top up saldo coin : ", "input tidak valid",
				"^[0-9]+(\\.[0-9]+)?$");
		double amount = Double.parseDouble(input);

		if (amount <= 0) {
			System.err.println("Input tidak valid");
		}

		member.setSaldoCoin(member.getSaldoCoin() + amount);
		System.out.println("Top up saldo coin berhasil");
		System.out.println("Saldo saat ini : " + member.getSaldoCoin());
	}

	// Informasi Booking Order
	public static void InformasiBookingOrder(List<Customer> listAllCustomers, List<BookingOrder> bookingOrders,
			String id) {
		Customer customer = findCustomer(listAllCustomers, id);
		if (customer == null) {
			System.err.println("Customer tidak ditemukan");
			return;
		}
		bookingOrders.stream()
				.filter(booking -> booking.getCustomer().getCustomerId().equals(customer.getCustomerId()))
				.forEach(booking -> {
					System.out.println("\n================================================================\n");
					System.out.println("No : " + (bookingOrders.indexOf(booking) + 1));
					System.out.println("Nama Customer : " + (booking.getCustomer().getName()));
					System.out.println("Payment Methods : " + booking.getPaymentMethod());
					System.out.println("Total Service : " + (booking.getTotalServicePrice()));
					System.out.println("Total Payment : " + (booking.getTotalPayment()));
					System.out.println("List services : ");
					booking.getServices().forEach(service -> {
						System.out.println(service.getServiceName() + ",");
					});
				});
	}

	// Logout
	public static void Logout() {
		System.exit(0);
	}

	private static Customer findCustomer(List<Customer> listAllCustomers, String id) {
		return listAllCustomers.stream()
				.filter(cust -> cust.getCustomerId().equals(id))
				.findFirst()
				.orElse(null);
	}

}
