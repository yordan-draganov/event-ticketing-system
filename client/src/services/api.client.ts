import { 
  EventsApi, 
  UsersApi, 
  PaymentsApi,
  TicketsApi,
  SeatsApi,
  SectionsApi
} from '../generated/api';
import { getApiConfig } from './api.config';
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

const getApis = () => {
  const config = getApiConfig();
  return {
    eventsApi: new EventsApi(config),
    usersApi: new UsersApi(config),
    paymentsApi: new PaymentsApi(config),
    ticketsApi: new TicketsApi(config),
    seatsApi: new SeatsApi(config),
    sectionsApi: new SectionsApi(config),
  };
};

export class ApiClient {
  static async getAllEvents(): Promise<EventResponse[]> {
    const { eventsApi } = getApis();
    const response = await eventsApi.getAllEvents();
    return response;
  }

  static async getEventById(id: string): Promise<EventResponse> {
    const { eventsApi } = getApis();
    const response = await eventsApi.getEventById({ id });
    return response;
  }

  static async createEvent(event: any): Promise<EventResponse> {
    const { eventsApi } = getApis();
    const response = await eventsApi.createEvent({ eventCreateDTO: event });
    return response;
  }

  static async updateEvent(id: string, event: any): Promise<EventResponse> {
    const { eventsApi } = getApis();
    const response = await eventsApi.updateEvent({ id, eventCreateDTO: event });
    return response;
  }

  static async deleteEvent(id: string): Promise<void> {
    const { eventsApi } = getApis();
    await eventsApi.deleteEvent({ id });
  }

  static async login(data: LoginRequest): Promise<AuthResponse> {
    const { usersApi } = getApis();
    const response = await usersApi.login({ loginRequest: data });

    console.log(response.token);
    
    if (response.token) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('userName', response.name);
      localStorage.setItem('userId', response.userId);
      localStorage.setItem('userEmail', response.email);
      localStorage.setItem('userRole', response.role);
    }
    
    return response;
  }

  static async signup(data: SignupRequest): Promise<AuthResponse> {
    const { usersApi } = getApis();
    const response = await usersApi.signUp({ signupRequest: data });
    
    if (response.token) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('userName', response.name);
      localStorage.setItem('userId', response.userId);
      localStorage.setItem('userEmail', response.email);
      localStorage.setItem('userRole', response.role);
    }
    
    return response;
  }

  static async logout(): Promise<string> {
    try {
      const { usersApi } = getApis();
      const response = await usersApi.logout();
      
      // Clear all stored data
      localStorage.removeItem('token');
      localStorage.removeItem('userName');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userRole');
      
      return response;
    } catch (error: any) {
      console.log('Logout API call failed, clearing local storage anyway:', error.message);
      
      localStorage.removeItem('token');
      localStorage.removeItem('userName');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userRole');
      
      return 'Logged out locally';
    }
  }

  static async getCurrentUser(): Promise<any> {
    const { usersApi } = getApis();
    const response = await usersApi.getCurrentUser();
    return response;
  }

  static async changeName(newName: string): Promise<AuthResponse> {
    const { usersApi } = getApis();
    const response = await usersApi.changeName({ 
      changeNameRequest: { newName } 
    });
    
    if (response.name) {
      localStorage.setItem('userName', response.name);
    }
    if (response.token) {
      localStorage.setItem('token', response.token);
    }
    
    return response;
  }

  static async changePassword(oldPassword: string, newPassword: string): Promise<string> {
    const { usersApi } = getApis();
    const response = await usersApi.changePassword({
      changePasswordRequest: { oldPassword, newPassword }
    });
    
    localStorage.removeItem('token');
    
    return response;
  }

  static async createPaymentIntent(data: PaymentDTO): Promise<PaymentResponse> {
    const { paymentsApi } = getApis();
    const response = await paymentsApi.createPaymentIntent({ 
      paymentDTO: data 
    });
    return response;
  }

  static async confirmPayment(paymentIntentId: string): Promise<any> {
    const { paymentsApi } = getApis();
    const response = await paymentsApi.confirmPayment({
      paymentConfirmDTO: { paymentIntentId }
    });
    return response;
  }

  static async getPaymentStatus(paymentIntentId: string): Promise<any> {
    const { paymentsApi } = getApis();
    const response = await paymentsApi.getPaymentStatus({ paymentIntentId });
    return response;
  }

  static async cancelPayment(paymentIntentId: string): Promise<string> {
    const { paymentsApi } = getApis();
    const response = await paymentsApi.cancelPayment({ paymentIntentId });
    return response;
  }

  static async createTicket(data: TicketCreateDTO): Promise<TicketResponse> {
    const { ticketsApi } = getApis();
    const response = await ticketsApi.createTicket({ 
      ticketCreateDTO: data 
    });
    return response;
  }

  static async getMyTickets(): Promise<TicketResponse[]> {
    const { ticketsApi } = getApis();
    const response = await ticketsApi.getMyTickets();
    return response;
  }

  static async getTicketById(id: string): Promise<TicketDetailResponse> {
    const { ticketsApi } = getApis();
    const response = await ticketsApi.getTicketById({ id });
    return response;
  }

  static async deleteTicket(id: string): Promise<string> {
    const { ticketsApi } = getApis();
    const response = await ticketsApi.deleteTicket({ id });
    return response;
  }

  static async getAllTickets(): Promise<TicketResponse[]> {
    const { ticketsApi } = getApis();
    const response = await ticketsApi.getAllTickets();
    return response;
  }

  static async getSeatsByEvent(eventId: string): Promise<any[]> {
    const { seatsApi } = getApis();
    const response = await seatsApi.getSeatsByEvent({ eventId });
    return response;
  }

  static async getSeatsBySection(sectionId: string): Promise<any[]> {
    const { seatsApi } = getApis();
    const response = await seatsApi.getSeatsBySection({ sectionId });
    return response;
  }

  static async getSectionsByEvent(eventId: string): Promise<any[]> {
    const { sectionsApi } = getApis();
    const response = await sectionsApi.getSectionsByEvent({ eventId });
    return response;
  }

  static async getSectionById(sectionId: string): Promise<any> {
    const { sectionsApi } = getApis();
    const response = await sectionsApi.getSectionById({ sectionId });
    return response;
  }
}