import jwt from "jsonwebtoken";
import User from "../models/User.js";

/**
 * JWT Authentication Middleware
 */
const authenticateToken = async (req, res, next) => {
  try {
    const authHeader = req.headers["authorization"]
    const token = authHeader && authHeader.split(" ")[1] // Bearer TOKEN

    if (!token) {
      return res.status(401).json({ error: "Access token required" })
    }

    const decoded = jwt.verify(token, process.env.JWT_SECRET)

    // Check if user still exists and is active
    const user = await User.findById(decoded.userId).select("-passwordHash")
    if (!user || !user.isActive) {
      return res.status(401).json({ error: "Invalid or expired token" })
    }

    req.user = user
    next()
  } catch (error) {
    if (error.name === "JsonWebTokenError") {
      return res.status(401).json({ error: "Invalid token" })
    }
    if (error.name === "TokenExpiredError") {
      return res.status(401).json({ error: "Token expired" })
    }

    console.error("Auth middleware error:", error)
    res.status(500).json({ error: "Authentication failed" })
  }
}

/**
 * Generate JWT token
 */
const generateToken = (userId) => {
  return jwt.sign(
    { userId },
    process.env.JWT_SECRET,
    { expiresIn: "30d" }, // Token expires in 30 days
  )
}

export { generateToken, authenticateToken };
