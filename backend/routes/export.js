import express from "express";
import { authenticateToken } from "../middleware/auth.js";
import { getExportData } from "../utils/export.js";

const router = express.Router()

// All routes require authentication
router.use(authenticateToken)

/**
 * @route GET /api/export/records
 * @desc Export milk records as text or CSV
 * @access Private
 */
router.get("/records", async (req, res) => {
  try {
    const { startDate, endDate, format = "text" } = req.query
    const userId = req.user._id

    if (!["text", "csv"].includes(format)) {
      return res.status(400).json({
        error: "Invalid format. Use 'text' or 'csv'",
      })
    }

    const exportData = await getExportData(userId, startDate, endDate, format)

    res.setHeader("Content-Type", exportData.contentType)
    res.setHeader("Content-Disposition", `attachment; filename="${exportData.filename}"`)

    res.send(exportData.data)
  } catch (error) {
    console.error("Export records error:", error)
    res.status(500).json({
      error: "Failed to export records",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route GET /api/export/share-link
 * @desc Generate shareable summary text
 * @access Private
 */
router.get("/share-link", async (req, res) => {
  try {
    const { startDate, endDate } = req.query
    const userId = req.user._id

    const exportData = await getExportData(userId, startDate, endDate, "text")

    res.json({
      summary: exportData.data,
      shareText: `Check out my milk delivery summary:\n\n${exportData.data}`,
      filename: exportData.filename,
    })
  } catch (error) {
    console.error("Share link error:", error)
    res.status(500).json({
      error: "Failed to generate share link",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

export default router;
