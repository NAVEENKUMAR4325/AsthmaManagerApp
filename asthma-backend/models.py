# asthma-backend/models.py

from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, Float, DateTime, Enum as SAEnum
from sqlalchemy.orm import relationship
from database import Base
import datetime
import enum

class UserRole(str, enum.Enum):
    PATIENT = "Patient"
    DOCTOR = "Doctor"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    name = Column(String, nullable=False)
    hashed_password = Column(String, nullable=False)
    role = Column(SAEnum(UserRole), nullable=False)
    
    # --- ADDED/UPDATED FIELDS ---
    age = Column(Integer, nullable=True)
    height = Column(Integer, nullable=True) # in cm
    gender = Column(String, nullable=True)
    contact_number = Column(String, nullable=True)
    address = Column(String, nullable=True)
    
    # Relationships
    baseline = relationship("BaselinePEFR", back_populates="owner", uselist=False)
    pefr_records = relationship("PEFRRecord", back_populates="owner")
    symptoms = relationship("Symptom", back_populates="owner")
    
    # --- NEW RELATIONSHIPS ---
    medications = relationship("Medication", back_populates="owner")
    emergency_contacts = relationship("EmergencyContact", back_populates="owner")
    reminders = relationship("Reminder", back_populates="owner")
    audit_logs = relationship("AuditLog", back_populates="user")
    alert_logs = relationship("AlertLog", back_populates="user")

class BaselinePEFR(Base):
    __tablename__ = "baseline_pefr"

    id = Column(Integer, primary_key=True, index=True)
    baseline_value = Column(Integer, nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="baseline")

class PEFRRecord(Base):
    __tablename__ = "pefr_records"

    id = Column(Integer, primary_key=True, index=True)
    pefr_value = Column(Integer, nullable=False)
    zone = Column(String, nullable=False)
    recorded_at = Column(DateTime, default=datetime.datetime.utcnow)
    owner_id = Column(Integer, ForeignKey("users.id"))
    
    # --- ADDED/UPDATED FIELDS ---
    percentage = Column(Float, nullable=True) # e.g., 85.5
    trend = Column(String, nullable=True) # e.g., "improving", "stable"
    source = Column(String, default="manual") # "manual" or "bluetooth"

    owner = relationship("User", back_populates="pefr_records")

class Symptom(Base):
    __tablename__ = "symptoms"
    
    id = Column(Integer, primary_key=True, index=True)
    wheeze_rating = Column(Integer) # Scale
    cough_rating = Column(Integer)  # Scale
    dust_exposure = Column(Boolean, default=False)
    smoke_exposure = Column(Boolean, default=False)
    recorded_at = Column(DateTime, default=datetime.datetime.utcnow)
    owner_id = Column(Integer, ForeignKey("users.id"))
    
    # --- ADDED/UPDATED FIELDS ---
    dyspnea_rating = Column(Integer, nullable=True) # Shortness of breath
    night_symptoms_rating = Column(Integer, nullable=True)
    severity = Column(String, nullable=True) # "None", "Mild", "Moderate", "Severe"
    onset_at = Column(DateTime, nullable=True)
    duration = Column(Integer, nullable=True) # in minutes
    suspected_trigger = Column(String, nullable=True)

    owner = relationship("User", back_populates="symptoms")

class DoctorPatient(Base):
    __tablename__ = "doctor_patient_map"
    
    id = Column(Integer, primary_key=True, index=True)
    doctor_id = Column(Integer, ForeignKey("users.id"))
    patient_id = Column(Integer, ForeignKey("users.id"))

# --- NEW TABLES ---

class Medication(Base):
    __tablename__ = "medications"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    dose = Column(String, nullable=True)
    schedule = Column(String, nullable=True) # e.g., "2 puffs twice daily"
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="medications")

class EmergencyContact(Base):
    __tablename__ = "emergency_contacts"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    phone_number = Column(String, nullable=False)
    
    # --- THIS IS THE FIX ---
    contact_relationship = Column(String, nullable=True) # e.g., "Spouse", "Parent"
    
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="emergency_contacts")

class Reminder(Base):
    __tablename__ = "reminders"
    id = Column(Integer, primary_key=True, index=True)
    reminder_type = Column(String, nullable=False) # "PEFR" or "Medication"
    time = Column(String, nullable=False) # e.g., "09:00"
    frequency = Column(String, nullable=False) # "Daily", "Weekly"
    compliance_count = Column(Integer, default=0)
    missed_count = Column(Integer, default=0)
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="reminders")

class AuditLog(Base):
    __tablename__ = "audit_logs"
    id = Column(Integer, primary_key=True, index=True)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
    user_id = Column(Integer, ForeignKey("users.id"))
    action = Column(String, nullable=False) # e.g., "LOGIN", "UPDATE_PROFILE"
    details = Column(String, nullable=True)
    
    user = relationship("User", back_populates="audit_logs")

class AlertLog(Base):
    __tablename__ = "alert_logs"
    id = Column(Integer, primary_key=True, index=True)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
    user_id = Column(Integer, ForeignKey("users.id"))
    alert_type = Column(String, nullable=False) # e.g., "RED_ZONE_TRIGGERED"
    resolved = Column(Boolean, default=False)

    user = relationship("User", back_populates="alert_logs")