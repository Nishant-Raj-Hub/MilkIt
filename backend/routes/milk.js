import express from "express";
import MilkRecord from "../models/MilkRecord.js";
import { authenticateToken } from "../middleware/auth.js";
import { validateDate, validateMilkQuantity } from "../utils/validation.js";

const router = express.Router()

// All routes require authentication
router.use(authenticateToken)

/**
 * @route POST /api/milk/add
 * @desc Add or update a milk record for a specific date
 * @access Private
 */
router.post("/add", async (req, res) => {
  try {
    const { date, liters, status, notes, milkType } = req.body
    const userId = req.user._id

    // Validation
    const dateValidation = validateDate(date)
    if (!dateValidation.isValid) {
      return res.status(400).json({
        error: "Invalid date",
        details: dateValidation.errors,
      })
    }

    const quantityValidation = validateMilkQuantity(liters)
    if (!quantityValidation.isValid) {
      return res.status(400).json({
        error: "Invalid milk quantity",
        details: quantityValidation.errors,
      })
    }

    // Parse and normalize date (remove time component)
    const recordDate = new Date(date)
    recordDate.setHours(0, 0, 0, 0)

    // Check if record already exists for this date
    let milkRecord = await MilkRecord.findOne({
      userId,
      date: recordDate,
    })

    if (milkRecord) {
      // Update existing record
      milkRecord.liters = liters || milkRecord.liters
      milkRecord.status = status || milkRecord.status
      milkRecord.notes = notes !== undefined ? notes : milkRecord.notes
      milkRecord.milkType = milkType || milkRecord.milkType
      milkRecord.isAutoMarked = false // User confirmed/updated
      milkRecord.updatedAt = new Date()

      await milkRecord.save()

      res.json({
        message: "Milk record updated successfully",
        record: milkRecord,
      })
    } else {
      // Create new record
      milkRecord = new MilkRecord({
        userId,
        date: recordDate,
        liters: liters || 1,
        status: status || "received",
        notes: notes || "",
        milkType: milkType || "cow",
        isAutoMarked: false, // User manually added
      })

      await milkRecord.save()

      res.status(201).json({
        message: "Milk record created successfully",
        record: milkRecord,
      })
    }
  } catch (error) {
    console.error("Add milk record error:", error)

    if (error.code === 11000) {
      return res.status(409).json({
        error: "Record already exists for this date",
      })
    }

    res.status(500).json({
      error: "Failed to add milk record",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route GET /api/milk/get
 * @desc Get milk records for a date range
 * @access Private
 */
router.get("/get", async (req, res) => {
  try {
    const { startDate, endDate, page = 1, limit = 50 } = req.query
    const userId = req.user._id

    // Build query
    const query = { userId }

    if (startDate || endDate) {
      query.date = {}
      if (startDate) {
        const start = new Date(startDate)
        start.setHours(0, 0, 0, 0)
        query.date.$gte = start
      }
      if (endDate) {
        const end = new Date(endDate)
        end.setHours(23, 59, 59, 999)
        query.date.$lte = end
      }
    }

    // Pagination
    const pageNum = Math.max(1, Number.parseInt(page))
    const limitNum = Math.min(100, Math.max(1, Number.parseInt(limit)))
    const skip = (pageNum - 1) * limitNum

    // Get records with pagination
    const records = await MilkRecord.find(query)
      .sort({ date: -1 }) // Most recent first
      .skip(skip)
      .limit(limitNum)
      .lean()

    // Get total count for pagination
    const totalRecords = await MilkRecord.countDocuments(query)
    const totalPages = Math.ceil(totalRecords / limitNum)

    // Calculate statistics
    const stats = await MilkRecord.aggregate([
      { $match: query },
      {
        $group: {
          _id: null,
          totalLiters: { $sum: "$liters" },
          averageLiters: { $avg: "$liters" },
          receivedCount: {
            $sum: { $cond: [{ $eq: ["$status", "received"] }, 1, 0] },
          },
          notReceivedCount: {
            $sum: { $cond: [{ $eq: ["$status", "not_received"] }, 1, 0] },
          },
          partialCount: {
            $sum: { $cond: [{ $eq: ["$status", "partial"] }, 1, 0] },
          },
          autoMarkedCount: {
            $sum: { $cond: ["$isAutoMarked", 1, 0] },
          },
        },
      },
    ])

    res.json({
      records,
      pagination: {
        currentPage: pageNum,
        totalPages,
        totalRecords,
        hasNextPage: pageNum < totalPages,
        hasPrevPage: pageNum > 1,
      },
      statistics: stats[0] || {
        totalLiters: 0,
        averageLiters: 0,
        receivedCount: 0,
        notReceivedCount: 0,
        partialCount: 0,
        autoMarkedCount: 0,
      },
    })
  } catch (error) {
    console.error("Get milk records error:", error)
    res.status(500).json({
      error: "Failed to fetch milk records",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route GET /api/milk/today
 * @desc Get today's milk record
 * @access Private
 */
router.get("/today", async (req, res) => {
  try {
    const userId = req.user._id
    const today = new Date()
    today.setHours(0, 0, 0, 0)

    const todayRecord = await MilkRecord.findOne({
      userId,
      date: today,
    })

    if (!todayRecord) {
      // Auto-create today's record if it doesn't exist
      const newRecord = new MilkRecord({
        userId,
        date: today,
        liters: 1,
        status: "received",
        isAutoMarked: true,
      })

      await newRecord.save()

      return res.json({
        record: newRecord,
        isNew: true,
        message: "Auto-created today's record",
      })
    }

    res.json({
      record: todayRecord,
      isNew: false,
    })
  } catch (error) {
    console.error("Get today's record error:", error)
    res.status(500).json({
      error: "Failed to fetch today's record",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route PUT /api/milk/confirm/:id
 * @desc Confirm an auto-marked record
 * @access Private
 */
router.put("/confirm/:id", async (req, res) => {
  try {
    const { id } = req.params
    const userId = req.user._id

    const record = await MilkRecord.findOne({
      _id: id,
      userId,
    })

    if (!record) {
      return res.status(404).json({
        error: "Record not found",
      })
    }

    record.isAutoMarked = false
    record.updatedAt = new Date()
    await record.save()

    res.json({
      message: "Record confirmed successfully",
      record,
    })
  } catch (error) {
    console.error("Confirm record error:", error)
    res.status(500).json({
      error: "Failed to confirm record",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route DELETE /api/milk/:id
 * @desc Delete a milk record
 * @access Private
 */
router.delete("/:id", async (req, res) => {
  try {
    const { id } = req.params
    const userId = req.user._id

    const record = await MilkRecord.findOneAndDelete({
      _id: id,
      userId,
    })

    if (!record) {
      return res.status(404).json({
        error: "Record not found",
      })
    }

    res.json({
      message: "Record deleted successfully",
      deletedRecord: record,
    })
  } catch (error) {
    console.error("Delete record error:", error)
    res.status(500).json({
      error: "Failed to delete record",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route GET /api/milk/monthly-stats/:year/:month
 * @desc Get monthly statistics
 * @access Private
 */
router.get("/monthly-stats/:year/:month", async (req, res) => {
  try {
    const { year, month } = req.params
    const userId = req.user._id

    const startDate = new Date(Number.parseInt(year), Number.parseInt(month) - 1, 1)
    const endDate = new Date(Number.parseInt(year), Number.parseInt(month), 0, 23, 59, 59, 999)

    const stats = await MilkRecord.aggregate([
      {
        $match: {
          userId,
          date: { $gte: startDate, $lte: endDate },
        },
      },
      {
        $group: {
          _id: {
            day: { $dayOfMonth: "$date" },
            status: "$status",
          },
          count: { $sum: 1 },
          totalLiters: { $sum: "$liters" },
        },
      },
      {
        $group: {
          _id: "$_id.day",
          statuses: {
            $push: {
              status: "$_id.status",
              count: "$count",
              totalLiters: "$totalLiters",
            },
          },
          dailyTotal: { $sum: "$totalLiters" },
        },
      },
      { $sort: { _id: 1 } },
    ])

    // Calculate overall monthly stats
    const overallStats = await MilkRecord.aggregate([
      {
        $match: {
          userId,
          date: { $gte: startDate, $lte: endDate },
        },
      },
      {
        $group: {
          _id: null,
          totalLiters: { $sum: "$liters" },
          averageLiters: { $avg: "$liters" },
          totalDays: { $sum: 1 },
          receivedDays: {
            $sum: { $cond: [{ $eq: ["$status", "received"] }, 1, 0] },
          },
          missedDays: {
            $sum: { $cond: [{ $eq: ["$status", "not_received"] }, 1, 0] },
          },
          partialDays: {
            $sum: { $cond: [{ $eq: ["$status", "partial"] }, 1, 0] },
          },
        },
      },
    ])

    res.json({
      year: Number.parseInt(year),
      month: Number.parseInt(month),
      dailyStats: stats,
      monthlyOverview: overallStats[0] || {
        totalLiters: 0,
        averageLiters: 0,
        totalDays: 0,
        receivedDays: 0,
        missedDays: 0,
        partialDays: 0,
      },
    })
  } catch (error) {
    console.error("Monthly stats error:", error)
    res.status(500).json({
      error: "Failed to fetch monthly statistics",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route POST /api/milk/bulk-create
 * @desc Create multiple records (for testing or data migration)
 * @access Private
 */
router.post("/bulk-create", async (req, res) => {
  try {
    const { records } = req.body
    const userId = req.user._id

    if (!Array.isArray(records) || records.length === 0) {
      return res.status(400).json({
        error: "Records array is required",
      })
    }

    if (records.length > 100) {
      return res.status(400).json({
        error: "Cannot create more than 100 records at once",
      })
    }

    // Validate and prepare records
    const validRecords = []
    const errors = []

    for (let i = 0; i < records.length; i++) {
      const record = records[i]
      const dateValidation = validateDate(record.date)
      const quantityValidation = validateMilkQuantity(record.liters)

      if (!dateValidation.isValid || !quantityValidation.isValid) {
        errors.push({
          index: i,
          errors: [...dateValidation.errors, ...quantityValidation.errors],
        })
        continue
      }

      const recordDate = new Date(record.date)
      recordDate.setHours(0, 0, 0, 0)

      validRecords.push({
        userId,
        date: recordDate,
        liters: record.liters || 1,
        status: record.status || "received",
        notes: record.notes || "",
        milkType: record.milkType || "cow",
        isAutoMarked: record.isAutoMarked !== undefined ? record.isAutoMarked : false,
      })
    }

    if (errors.length > 0) {
      return res.status(400).json({
        error: "Validation errors in records",
        details: errors,
      })
    }

    // Use insertMany with ordered: false to continue on duplicates
    const result = await MilkRecord.insertMany(validRecords, {
      ordered: false,
    })

    res.status(201).json({
      message: `Successfully created ${result.length} records`,
      createdRecords: result.length,
      totalRequested: records.length,
    })
  } catch (error) {
    console.error("Bulk create error:", error)

    if (error.code === 11000) {
      // Handle duplicate key errors
      const insertedCount = error.result?.result?.insertedCount || 0
      return res.status(207).json({
        message: `Partially successful: ${insertedCount} records created`,
        error: "Some records already exist for the specified dates",
        insertedCount,
      })
    }

    res.status(500).json({
      error: "Failed to create records",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

export default router;
