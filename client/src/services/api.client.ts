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
  EventCreateDTO,
  LoginRequest,
  SignupRequest,
  AuthResponse,
  PaymentDTO,
  PaymentResponse,
  PaymentStatusResponse,
  TicketCreateDTO,
  TicketResponse,
  TicketDetailResponse,
  UserDTO,
  SeatResponse,
  SectionResponse
} from '../generated/api';

const safeSetStorageItem = (key: string, value: string) => {
  try {
    localStorage.setItem(key, value);
  } catch (error) {
    console.error(`Failed to save ${key} to local storage:`, error);
  }
};

const safeRemoveStorageItem = (key: string) => {
  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.error(`Failed to remove ${key} from local storage:`, error);
  }
};

const persistAuthSession = (response: AuthResponse) => {
  if (!response.name) return;

  safeSetStorageItem('userName', response.name);
  safeSetStorageItem('userId', response.userId || '');
  safeSetStorageItem('userEmail', response.email || '');
  safeSetStorageItem('userRole', response.role || 'user');
};

const clearAuthSession = () => {
  safeRemoveStorageItem('userName');
  safeRemoveStorageItem('userId');
  safeRemoveStorageItem('userEmail');
  safeRemoveStorageItem('userRole');
};

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

  static async createEvent(event: EventCreateDTO): Promise<EventResponse> {
    const { eventsApi } = getApis();
    const response = await eventsApi.createEvent({ eventCreateDTO: event });
    return response;
  }

  static async updateEvent(id: string, event: EventCreateDTO): Promise<EventResponse> {
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

    persistAuthSession(response);

    return response;
  }

  static async signup(data: SignupRequest): Promise<AuthResponse> {
    const { usersApi } = getApis();
    const response = await usersApi.signUp({ signupRequest: data });

    persistAuthSession(response);

    return response;
  }

  static async logout(): Promise<string> {
    try {
      const { usersApi } = getApis();
      const response = await usersApi.logout();

      clearAuthSession();

      return response;
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : 'Unknown error';
      console.log('Logout completed:', errorMessage);

      clearAuthSession();

      return 'Logged out locally';
    }
  }

  static async getCurrentUser(): Promise<UserDTO> {
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
      safeSetStorageItem('userName', response.name);
    }

    return response;
  }

  static async changePassword(oldPassword: string, newPassword: string): Promise<string> {
    const { usersApi } = getApis();
    const response = await usersApi.changePassword({
      changePasswordRequest: { oldPassword, newPassword }
    });

    clearAuthSession();

    return response;
  }

  static async createPaymentIntent(data: PaymentDTO): Promise<PaymentResponse> {
    const { paymentsApi } = getApis();
    const response = await paymentsApi.createPaymentIntent({ 
      paymentDTO: data 
    });
    return response;
  }

  static async confirmPayment(paymentIntentId: string): Promise<object> {
    const { paymentsApi } = getApis();
    const response = await paymentsApi.confirmPayment({
      paymentConfirmDTO: { paymentIntentId }
    });
    return response;
  }

  static async getPaymentStatus(paymentIntentId: string): Promise<PaymentStatusResponse> {
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

  static async getSeatsByEvent(eventId: string): Promise<SeatResponse[]> {
    const { seatsApi } = getApis();
    const response = await seatsApi.getSeatsByEvent({ eventId });
    return response;
  }

  static async getSeatsBySection(sectionId: string): Promise<SeatResponse[]> {
    const { seatsApi } = getApis();
    const response = await seatsApi.getSeatsBySection({ sectionId });
    return response;
  }

  static async getSectionsByEvent(eventId: string): Promise<SectionResponse[]> {
    const { sectionsApi } = getApis();
    const response = await sectionsApi.getSectionsByEvent({ eventId });
    return response;
  }

  static async getSectionById(sectionId: string): Promise<SectionResponse> {
    const { sectionsApi } = getApis();
    const response = await sectionsApi.getSectionById({ sectionId });
    return response;
  }
}
