import { 
  EventsApi, 
  UsersApi, 
  PaymentsApi,
  TicketsApi,
  SeatsApi,
  SectionsApi
} from '../generated/api';
import { apiConfig } from './api.config';
import type {
  EventResponse,
  LoginRequest,
  SignupRequest,
  AuthResponse,
  PaymentDTO,
  PaymentResponse,
  TicketCreateDTO,
  TicketResponse,
  TicketDetailResponse
} from '../generated/api';

const eventsApi = new EventsApi(apiConfig);
const usersApi = new UsersApi(apiConfig);
const paymentsApi = new PaymentsApi(apiConfig);
const ticketsApi = new TicketsApi(apiConfig);
const seatsApi = new SeatsApi(apiConfig);
const sectionsApi = new SectionsApi(apiConfig);

export class ApiClient {
  static async getAllEvents(): Promise<EventResponse[]> {
    const response = await eventsApi.getAllEvents();
    return response;
  }

  static async getEventById(id: string): Promise<EventResponse> {
    const response = await eventsApi.getEventById({ id });
    return response;
  }

  static async createEvent(event: any): Promise<EventResponse> {
    const response = await eventsApi.createEvent({ eventCreateDTO: event });
    return response;
  }

  static async updateEvent(id: string, event: any): Promise<EventResponse> {
    const response = await eventsApi.updateEvent({ id, eventCreateDTO: event });
    return response;
  }

  static async deleteEvent(id: string): Promise<void> {
    await eventsApi.deleteEvent({ id });
  }

  static async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await usersApi.login({ loginRequest: data });
    
    if (response.token) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('userName', response.name);
      localStorage.setItem('userId', response.userId);
    }
    
    return response;
  }

  static async signup(data: SignupRequest): Promise<AuthResponse> {
    const response = await usersApi.signUp({ signupRequest: data });
    
    if (response.token) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('userName', response.name);
      localStorage.setItem('userId', response.userId);
    }
    
    return response;
  }

  static async logout(): Promise<string> {
    const response = await usersApi.logout();
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    localStorage.removeItem('userId');
    return response;
  }

  static async getCurrentUser(): Promise<any> {
    const response = await usersApi.getCurrentUser();
    return response;
  }

  static async changeName(newName: string): Promise<AuthResponse> {
    const response = await usersApi.changeName({ 
      changeNameRequest: { newName } 
    });
    if (response.name) {
      localStorage.setItem('userName', response.name);
    }
    return response;
  }

  static async changePassword(oldPassword: string, newPassword: string): Promise<string> {
    const response = await usersApi.changePassword({
      changePasswordRequest: { oldPassword, newPassword }
    });
    return response;
  }

  // ==================== PAYMENTS ====================
  static async createPaymentIntent(data: PaymentDTO): Promise<PaymentResponse> {
    const response = await paymentsApi.createPaymentIntent({ 
      paymentDTO: data 
    });
    return response;
  }

  static async confirmPayment(paymentIntentId: string): Promise<any> {
    const response = await paymentsApi.confirmPayment({
      paymentConfirmDTO: { paymentIntentId }
    });
    return response;
  }

  static async getPaymentStatus(paymentIntentId: string): Promise<any> {
    const response = await paymentsApi.getPaymentStatus({ paymentIntentId });
    return response;
  }

  static async cancelPayment(paymentIntentId: string): Promise<string> {
    const response = await paymentsApi.cancelPayment({ paymentIntentId });
    return response;
  }

  static async createTicket(data: TicketCreateDTO): Promise<TicketResponse> {
    const response = await ticketsApi.createTicket({ 
      ticketCreateDTO: data 
    });
    return response;
  }

  static async getMyTickets(): Promise<TicketResponse[]> {
    const response = await ticketsApi.getMyTickets();
    return response;
  }

  static async getTicketById(id: string): Promise<TicketDetailResponse> {
    const response = await ticketsApi.getTicketById({ id });
    return response;
  }

  static async deleteTicket(id: string): Promise<string> {
    const response = await ticketsApi.deleteTicket({ id });
    return response;
  }

  static async getAllTickets(): Promise<TicketResponse[]> {
    const response = await ticketsApi.getAllTickets();
    return response;
  }

  static async getSeatsByEvent(eventId: string): Promise<any[]> {
    const response = await seatsApi.getSeatsByEvent({ eventId });
    return response;
  }

  static async getSeatsBySection(sectionId: string): Promise<any[]> {
    const response = await seatsApi.getSeatsBySection({ sectionId });
    return response;
  }

  // ==================== SECTIONS ====================
  static async getSectionsByEvent(eventId: string): Promise<any[]> {
    const response = await sectionsApi.getSectionsByEvent({ eventId });
    return response;
  }

  static async getSectionById(sectionId: string): Promise<any> {
    const response = await sectionsApi.getSectionById({ sectionId });
    return response;
  }
}