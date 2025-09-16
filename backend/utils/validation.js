/**
 * Validation utilities for the MilkIt app
 */

/**
 * Validate username
 */
const validateUsername = (username) => {
  const errors = []

  if (!username) {
    errors.push("Username is required")
  } else {
    if (username.length < 3) {
      errors.push("Username must be at least 3 characters long")
    }
    if (username.length > 30) {
      errors.push("Username must be no more than 30 characters long")
    }
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      errors.push("Username can only contain letters, numbers, and underscores")
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
  }
}

/**
 * Validate phone number
 */
const validatePhone = (phone) => {
  const errors = []

  if (!phone) {
    errors.push("Phone number is required")
  } else {
    if (!/^[0-9]{10}$/.test(phone)) {
      errors.push("Phone number must be exactly 10 digits")
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
  }
}

/**
 * Validate password
 */
const validatePassword = (password) => {
  const errors = []

  if (!password) {
    errors.push("Password is required")
  } else {
    if (password.length < 6) {
      errors.push("Password must be at least 6 characters long")
    }
    if (password.length > 128) {
      errors.push("Password must be no more than 128 characters long")
    }
    // Optional: Add more password strength requirements
    // if (!/(?=.*[a-z])/.test(password)) {
    //   errors.push("Password must contain at least one lowercase letter")
    // }
    // if (!/(?=.*[A-Z])/.test(password)) {
    //   errors.push("Password must contain at least one uppercase letter")
    // }
    // if (!/(?=.*\d)/.test(password)) {
    //   errors.push("Password must contain at least one number")
    // }
  }

  return {
    isValid: errors.length === 0,
    errors,
  }
}

/**
 * Validate date
 */
const validateDate = (date) => {
  const errors = []

  if (!date) {
    errors.push("Date is required")
  } else {
    const parsedDate = new Date(date)
    if (isNaN(parsedDate.getTime())) {
      errors.push("Invalid date format")
    } else {
      const now = new Date()
      const oneYearAgo = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())
      const oneYearFromNow = new Date(now.getFullYear() + 1, now.getMonth(), now.getDate())

      if (parsedDate < oneYearAgo) {
        errors.push("Date cannot be more than one year in the past")
      }
      if (parsedDate > oneYearFromNow) {
        errors.push("Date cannot be more than one year in the future")
      }
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
  }
}

/**
 * Validate milk quantity
 */
const validateMilkQuantity = (liters) => {
  const errors = []

  if (liters === undefined || liters === null) {
    errors.push("Milk quantity is required")
  } else {
    const quantity = Number.parseFloat(liters)
    if (isNaN(quantity)) {
      errors.push("Milk quantity must be a valid number")
    } else {
      if (quantity < 0) {
        errors.push("Milk quantity cannot be negative")
      }
      if (quantity > 50) {
        errors.push("Milk quantity cannot exceed 50 liters")
      }
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
  }
}

module.exports = {
  validateUsername,
  validatePhone,
  validatePassword,
  validateDate,
  validateMilkQuantity,
}
